package allMains;

import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import circlePack.PackControl;
import frames.OwlSplashScreen;

/**
 * Single-process entry point for CirclePack, using OwlSplashScreen.
 *
 * This REPLACES both SplashMain.java and RunCirclePack.java. The old chain
 * was: RunCirclePack extracts cpcore.jar to a fresh temp directory and
 * relaunches a second JVM from there; that second JVM's Main-Class,
 * SplashMain, shows a splash (frames.SplashFrame), reflectively loads
 * CP_after_Splash (whose static field construction of CirclePack(1) -
 * i.e. "new PackControl()" + "initPackControl()" - therefore runs on the
 * plain main thread, not the EDT), tears down the splash immediately
 * afterward, then hands off to CP_after_Splash.main(), which parses args
 * and defers startCirclePack() + populateDisplay() onto the EDT.
 *
 * IMPORTANT: PackControl.resetDisplay() - called at the very end of
 * initPackControl(), i.e. at the very end of "new CirclePack(1)" - is what
 * calls frame.setVisible(true). So in the OLD chain, the real CirclePack
 * window already becomes visible during construction, and the splash gets
 * torn down right after, BEFORE startCirclePack()/populateDisplay() ever
 * run. That's the empty-window-then-20-second-wait gap. This version fixes
 * that: the splash stays up and on top for the WHOLE sequence - window
 * construction, script loading, and display population - and is only
 * disposed once populateDisplay() actually finishes, so the user never
 * sees the empty window at all.
 *
 * Threading: doInBackground() constructs CirclePack(1)/PackControl off the
 * EDT, matching what the static-field-init did before (this is a pre-
 * existing, if not strictly by-the-book, pattern in this codebase -
 * initPackControl() builds a large amount of Swing UI directly; changing
 * that to be fully EDT-correct would be a much larger refactor of
 * PackControl itself, out of scope here). done() - guaranteed by
 * SwingWorker to run on the EDT - calls startCirclePack() + populateDisplay(),
 * matching the old invokeLater block, then disposes the splash.
 *
 * No second JVM, no temp-directory jar copy, no reflection.
 *
 * Now that the project targets Java 17 (bumped from 1.8), this uses a
 * record for ProgressUpdate and the diamond operator on the SwingWorker
 * anonymous class - both rejected under -source 8, which is why an earlier
 * revision of this file used a plain class and explicit type arguments
 * instead.
 */
public class CirclePackMain {

    public static void main(String[] args) {
        // Parse command-line args up front, same logic as
        // CP_after_Splash.main() used to do. These fields (CPBase.directory,
        // CPBase.initialScript, CPBase.socketActive, CPBase.cpSocketPort)
        // are read later inside startCirclePack(), so they must be set
        // before done() runs below - doing it here, before the splash
        // even shows, guarantees that.
        parseArgs(args);

        SwingUtilities.invokeLater(() -> {
            OwlSplashScreen splash = new OwlSplashScreen();
            // Guarantee the splash stays on top even though PackControl's
            // real window becomes visible (via resetDisplay()) partway
            // through the background work below - otherwise the OS/window
            // manager could bring that window to front over the splash.
            splash.setAlwaysOnTop(true);
            splash.setVisible(true);

            SwingWorker<CirclePack, ProgressUpdate> worker = new SwingWorker<>() {
                @Override
                protected CirclePack doInBackground() throws Exception {
                    publish(new ProgressUpdate("Starting CirclePack...", 40));
                    // Same call CP_after_Splash's static field used to make,
                    // just now explicit and deliberately off the EDT rather
                    // than as a side effect of Class.forName().
                    CirclePack circlePack = new CirclePack(1);
                    publish(new ProgressUpdate("Loading script...", 80));
                    return circlePack;
                }

                @Override
                protected void process(List<ProgressUpdate> chunks) {
                    ProgressUpdate latest = chunks.get(chunks.size() - 1);
                    splash.setStatus(latest.message(), latest.percent());
                }

                @Override
                protected void done() {
                    try {
                        CirclePack circlePack = get(); // re-throws doInBackground()'s exception, if any
                        System.out.println("CirclePack started\n");
                        // Same as CP_after_Splash's EventQueue.invokeLater
                        // block - done() already runs on the EDT, so no
                        // extra invokeLater wrapping is needed here.
                        circlePack.startCirclePack();
                        PackControl.scriptManager.populateDisplay();
                    } catch (Exception e) {
                        e.printStackTrace();
                        // TODO: show an error dialog rather than failing silently
                    } finally {
                        // Only now - after the window is actually populated -
                        // does the splash come down. This is the key fix: no
                        // more empty-window gap.
                        splash.dispose();
                    }
                }
            };
            worker.execute();
        });
    }

    // Same argument parsing CP_after_Splash.main() used to do.
    private static void parseArgs(String[] args) {
        if (args.length >= 1) {
            for (int j = 0; j < args.length; j++) {
                if (args[j].equals("-dir") && args.length > j + 1) {
                    CPBase.directory = args[j + 1];
                    j++;
                } else if (args[j].startsWith("-scr") && args.length > j + 1) {
                    CPBase.initialScript = args[j + 1];
                    j++;
                } else if (args[j].equals("-socket")) {
                    CPBase.socketActive = true;
                    int prt = 3736;
                    try {
                        prt = Integer.parseInt(args[j + 1]);
                        CPBase.cpSocketPort = prt;
                        j++;
                    } catch (Exception ex) {
                        prt = 3736;
                    }
                } else if (j == args.length - 1) {
                    CPBase.initialScript = args[j];
                }
            }
        }
    }

    private record ProgressUpdate(String message, int percent) {}
}
