package infoProcessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @brief Generate the 'CmdGuide.txt' resource: command -> {guide URL, examples}.
 *
 * Standalone generator (run from the project root) that scans the
 * MyST user-guide markdown in 'docs/user_guide' and, for each known
 * CirclePack command, extracts:
 * <ul>
 * <li>up to 3 usage examples (lines inside code fences whose first token
 *     is the command name), and</li>
 * <li>a link to that command's page in the published user guide
 *     (GitHub Pages, base {@link #BASE_URL}), with a best-effort
 *     section anchor.</li>
 * </ul>
 * Output is one TAB-separated line per command:
 * <pre>command\tURL\texample1[\texample2[\texample3]]</pre>
 * written to 'src/Resources/doc/CmdGuide.txt'. It is loaded at runtime by
 * {@link input.MyConsole} and shown beneath the usage line on TAB completion.
 *
 * The set of "known commands" is taken from the first token of each line
 * of 'CmdCompletion.txt', so this stays in step with TAB completion.
 *
 * @author Claude
 */
public class CmdGuideGen {

	/** Published user guide base; see .github/workflows/deploy-docs.yml (BASE_URL=/CirclePack/guide). */
	static final String BASE_URL = "https://ashejim.github.io/CirclePack/guide";
	static final int MAX_EXAMPLES = 3;
	static final int MAX_EX_LEN = 140;

	// matches the first `backticked` token in a heading
	static final Pattern BACKTICK = Pattern.compile("`([^`]+)`");

	/** accumulates data for one command */
	static class Entry {
		String url;
		List<String> exampleBucket = new ArrayList<String>(); // from "### Examples"
		List<String> otherBucket = new ArrayList<String>();   // other (non-synopsis) fences
	}

	public static void main(String[] args) throws Exception {
		File root = new File(args.length > 0 ? args[0] : ".");
		File completion = new File(root, "src/Resources/doc/CmdCompletion.txt");
		File guideDir = new File(root, "docs/user_guide");
		File out = new File(root, "src/Resources/doc/CmdGuide.txt");

		Set<String> known = readKnownCommands(completion);
		System.out.println("known commands: " + known.size());

		// first-wins map; scan dedicated *_command.md pages first so core
		// commands get their own page URL, then the reference pages.
		LinkedHashMap<String, Entry> data = new LinkedHashMap<String, Entry>();

		List<File> files = orderedMdFiles(guideDir);
		for (File f : files)
			scanFile(f, known, data);

		// write the data file
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		int written = 0;
		for (String cmd : data.keySet()) {
			Entry e = data.get(cmd);
			List<String> ex = pickExamples(e);
			if (e.url == null)
				continue;
			StringBuilder sb = new StringBuilder(cmd).append('\t').append(e.url);
			for (String x : ex)
				sb.append('\t').append(x);
			bw.write(sb.toString());
			bw.newLine();
			written++;
		}
		bw.close();
		System.out.println("wrote " + written + " command entries to " + out.getPath());
	}

	static Set<String> readKnownCommands(File f) throws Exception {
		Set<String> set = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0)
				continue;
			int sp = line.indexOf(' ');
			set.add(sp < 0 ? line : line.substring(0, sp));
		}
		br.close();
		return set;
	}

	/** Scan order (first-wins): dedicated '<cmd>_command.md' pages, then
	 * '*_reference.md' canonical reference pages, then everything else
	 * (tutorial, concepts, ...). This keeps each command's link pointed at
	 * its most authoritative page. */
	static List<File> orderedMdFiles(File dir) {
		List<File> dedicated = new ArrayList<File>();
		List<File> reference = new ArrayList<File>();
		List<File> rest = new ArrayList<File>();
		File[] all = dir.listFiles();
		if (all == null)
			return dedicated;
		for (File f : all) {
			if (!f.isFile() || !f.getName().endsWith(".md"))
				continue;
			if (f.getName().endsWith("_command.md"))
				dedicated.add(f);
			else if (f.getName().endsWith("_reference.md"))
				reference.add(f);
			else
				rest.add(f);
		}
		dedicated.addAll(reference);
		dedicated.addAll(rest);
		return dedicated;
	}

	static void scanFile(File f, Set<String> known, HashMap<String, Entry> data)
			throws Exception {
		String name = f.getName();
		String slug = slugify(name.substring(0, name.length() - 3)); // drop ".md"

		// dedicated command page? e.g. disp_command.md -> "disp"
		String dedicatedCmd = null;
		if (name.endsWith("_command.md")) {
			String cand = name.substring(0, name.length() - "_command.md".length());
			if (known.contains(cand))
				dedicatedCmd = cand;
		}

		String currentCmd = dedicatedCmd;
		String currentUrl = (dedicatedCmd != null) ? BASE_URL + "/" + slug : null;
		int subKind = 0; // 0=normal, 1=synopsis (skip), 2=examples
		boolean inFence = false;

		BufferedReader br = new BufferedReader(new FileReader(f));
		String line;
		while ((line = br.readLine()) != null) {
			String trimmed = line.trim();

			// code-fence toggle
			if (trimmed.startsWith("```")) {
				inFence = !inFence;
				continue;
			}

			if (!inFence && trimmed.startsWith("#")) {
				// heading line
				String text = trimmed.replaceFirst("^#+\\s*", "");
				String cand = firstBacktickToken(text);
				if (cand != null && known.contains(cand)) {
					currentCmd = cand;
					if (dedicatedCmd != null && cand.equals(dedicatedCmd))
						currentUrl = BASE_URL + "/" + slug; // page top
					else
						currentUrl = BASE_URL + "/" + slug + "#" + anchor(text);
					subKind = 0;
				} else {
					// subsection within current command
					String low = text.toLowerCase();
					if (low.contains("synopsis"))
						subKind = 1;
					else if (low.startsWith("examples") || low.equals("example"))
						subKind = 2;
					else
						subKind = 0;
				}
				continue;
			}

			// example collection inside a (non-synopsis) fence
			if (inFence && currentCmd != null && subKind != 1) {
				String first = firstToken(trimmed);
				if (first != null && first.equals(currentCmd)) {
					Entry e = data.get(currentCmd);
					if (e == null) {
						e = new Entry();
						e.url = currentUrl;
						data.put(currentCmd, e);
					}
					String ex = cleanExample(trimmed);
					List<String> bucket = (subKind == 2) ? e.exampleBucket : e.otherBucket;
					if (!bucket.contains(ex) && !e.exampleBucket.contains(ex)
							&& !e.otherBucket.contains(ex))
						bucket.add(ex);
				}
			}
		}
		br.close();
	}

	/** Prefer concrete examples (no placeholder braces/brackets) over
	 * abstract synopsis-style lines; within that, "### Examples" first. */
	static List<String> pickExamples(Entry e) {
		List<String> out = new ArrayList<String>();
		List<String> ordered = new ArrayList<String>();
		ordered.addAll(e.exampleBucket);
		ordered.addAll(e.otherBucket);
		// pass 1: concrete examples
		for (String x : ordered) {
			if (out.size() >= MAX_EXAMPLES)
				break;
			if (!isAbstract(x) && !out.contains(x))
				out.add(x);
		}
		// pass 2: fall back to abstract/synopsis forms if still short
		for (String x : ordered) {
			if (out.size() >= MAX_EXAMPLES)
				break;
			if (!out.contains(x))
				out.add(x);
		}
		return out;
	}

	/** A line is "abstract" (synopsis-like) if it carries placeholders. */
	static boolean isAbstract(String s) {
		return s.indexOf('{') >= 0 || s.indexOf('[') >= 0;
	}

	static String firstBacktickToken(String headingText) {
		Matcher m = BACKTICK.matcher(headingText);
		if (!m.find())
			return null;
		return firstToken(m.group(1).trim());
	}

	static String firstToken(String s) {
		s = s.trim();
		if (s.length() == 0)
			return null;
		int sp = s.indexOf(' ');
		return sp < 0 ? s : s.substring(0, sp);
	}

	/** MyST heading anchor: backticks stripped, then slugified. */
	static String anchor(String headingText) {
		return slugify(headingText.replace("`", ""));
	}

	/** MyST/Jupyter-Book slug: lowercase; every run of non-alphanumeric
	 * characters (spaces, underscores, punctuation) becomes a single '-';
	 * leading/trailing '-' trimmed. e.g. "set_aim — Set target angle sums"
	 * -> "set-aim-set-target-angle-sums", "solvers_reference" ->
	 * "solvers-reference". */
	static String slugify(String s) {
		s = s.toLowerCase();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'))
				sb.append(c);
			else
				sb.append('-');
		}
		String t = sb.toString().replaceAll("-+", "-");
		return t.replaceAll("^-+|-+$", "");
	}

	static String cleanExample(String s) {
		s = s.trim().replaceAll("\\s+", " ");
		if (s.length() > MAX_EX_LEN)
			s = s.substring(0, MAX_EX_LEN - 1) + "…";
		return s;
	}
}
