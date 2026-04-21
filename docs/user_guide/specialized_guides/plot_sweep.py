#!/usr/bin/env python3
"""
plot_sweep.py — Plot holonomy error landscape from |GB| sweep output.

Usage:
    python3 plot_sweep.py sweep_output.csv
    python3 plot_sweep.py sweep_output.csv -o landscape.png
    python3 plot_sweep.py sweep_output.csv --log   # log scale on z-axis
    python3 plot_sweep.py sweep_output.csv --contour  # contour plot instead of surface

Reads CSV with columns: x, y, holonomy_error
Produces a 3D surface plot and optionally a 2D contour plot.
"""

import argparse
import sys
import numpy as np
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
from matplotlib import cm


def load_data(filename):
    """Load sweep CSV data."""
    data = np.genfromtxt(filename, delimiter=',', skip_header=1, names=['x', 'y', 'z'])
    return data


def make_grid(data):
    """Convert scattered (x,y,z) data to grid arrays for surface plotting."""
    xs = np.sort(np.unique(data['x']))
    ys = np.sort(np.unique(data['y']))

    X, Y = np.meshgrid(xs, ys)
    Z = np.full_like(X, np.nan)

    # Build lookup
    lookup = {}
    for row in data:
        key = (round(row['x'], 10), round(row['y'], 10))
        lookup[key] = row['z']

    for i, y in enumerate(ys):
        for j, x in enumerate(xs):
            key = (round(x, 10), round(y, 10))
            if key in lookup:
                Z[i, j] = lookup[key]

    return X, Y, Z


def plot_surface(X, Y, Z, title="Holonomy Error Landscape",
                 log_scale=False, output=None):
    """3D surface plot."""
    fig = plt.figure(figsize=(12, 8))
    ax = fig.add_subplot(111, projection='3d')

    Zplot = Z.copy()
    if log_scale:
        Zplot = np.log10(np.maximum(Zplot, 1e-16))
        zlabel = "log10(holonomy error)"
    else:
        zlabel = "Holonomy Error (Frobenius norm)"

    # Mask NaN for plotting
    Zplot_masked = np.ma.array(Zplot, mask=np.isnan(Zplot))

    surf = ax.plot_surface(X, Y, Zplot_masked,
                           cmap=cm.viridis,
                           edgecolor='none',
                           alpha=0.9)

    ax.set_xlabel('x', fontsize=12)
    ax.set_ylabel('y', fontsize=12)
    ax.set_zlabel(zlabel, fontsize=10)
    ax.set_title(title, fontsize=14)

    fig.colorbar(surf, ax=ax, shrink=0.6, pad=0.1,
                 label=zlabel)

    if output:
        plt.savefig(output, dpi=150, bbox_inches='tight')
        print(f"Saved surface plot to {output}")
    else:
        plt.show()


def plot_contour(X, Y, Z, title="Holonomy Error Contours",
                 log_scale=False, output=None):
    """2D contour plot — useful for identifying zero curves."""
    fig, ax = plt.subplots(figsize=(10, 8))

    Zplot = Z.copy()
    if log_scale:
        Zplot = np.log10(np.maximum(Zplot, 1e-16))
        zlabel = "log10(holonomy error)"
    else:
        zlabel = "Holonomy Error"

    # Filled contours
    levels = 30
    cf = ax.contourf(X, Y, Zplot, levels=levels, cmap=cm.viridis)
    fig.colorbar(cf, ax=ax, label=zlabel)

    # Contour lines
    cs = ax.contour(X, Y, Zplot, levels=levels,
                    colors='white', linewidths=0.3, alpha=0.5)

    # Highlight near-zero contour if not log scale
    if not log_scale:
        zmin = np.nanmin(Zplot)
        zmax = np.nanmax(Zplot)
        threshold = zmin + 0.05 * (zmax - zmin)
        ax.contour(X, Y, Zplot, levels=[threshold],
                   colors='red', linewidths=2, linestyles='dashed')
        ax.set_title(title + f"\n(red dashed = near-minimum region, "
                     f"threshold={threshold:.4f})", fontsize=12)
    else:
        ax.set_title(title, fontsize=14)

    ax.set_xlabel('x', fontsize=12)
    ax.set_ylabel('y', fontsize=12)
    ax.set_aspect('equal')

    if output:
        base = output.rsplit('.', 1)
        contour_file = base[0] + '_contour.' + base[1] if len(base) > 1 \
            else output + '_contour'
        plt.savefig(contour_file, dpi=150, bbox_inches='tight')
        print(f"Saved contour plot to {contour_file}")
    else:
        plt.show()


def print_stats(data):
    """Print summary statistics."""
    z = data['z']
    print(f"\n--- Sweep Statistics ---")
    print(f"  Points:   {len(z)}")
    print(f"  Min error: {np.min(z):.8f}  at  "
          f"({data['x'][np.argmin(z)]:.6f}, "
          f"{data['y'][np.argmin(z)]:.6f})")
    print(f"  Max error: {np.max(z):.8f}")
    print(f"  Mean:      {np.mean(z):.8f}")
    print(f"  Median:    {np.median(z):.8f}")

    # Points near zero
    for thresh in [0.1, 0.01, 0.001]:
        near_zero = np.sum(z < thresh)
        print(f"  Points with error < {thresh}: {near_zero}")
    print()


def main():
    parser = argparse.ArgumentParser(
        description="Plot holonomy error landscape from |GB| sweep")
    parser.add_argument("csvfile", help="CSV file from |GB| sweep command")
    parser.add_argument("-o", "--output", default=None,
                        help="Output image file (e.g. landscape.png)")
    parser.add_argument("--log", action="store_true",
                        help="Use log scale on z-axis")
    parser.add_argument("--contour", action="store_true",
                        help="Also show 2D contour plot")
    parser.add_argument("--stats", action="store_true",
                        help="Print summary statistics")
    parser.add_argument("--title", default=None,
                        help="Custom plot title")
    args = parser.parse_args()

    data = load_data(args.csvfile)
    title = args.title or "Holonomy Error Landscape"

    if args.stats or True:  # always print stats
        print_stats(data)

    X, Y, Z = make_grid(data)

    plot_surface(X, Y, Z, title=title,
                 log_scale=args.log, output=args.output)

    if args.contour:
        plot_contour(X, Y, Z, title=title,
                     log_scale=args.log, output=args.output)


if __name__ == "__main__":
    main()
