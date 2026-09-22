#!/usr/bin/env python3
"""Compare AndroidX benchmark JSON from the same physical device and compilation mode."""
import argparse
import json
import math
import sys


def compare(before, after, tolerance):
    old = {b['name']: b for b in before['benchmarks']}
    regressions = []
    compared = 0
    for benchmark in after['benchmarks']:
        previous = old.get(benchmark['name'])
        if previous is None:
            continue
        for group in ('metrics', 'sampledMetrics'):
            for name, values in benchmark.get(group, {}).items():
                baseline = previous.get(group, {}).get(name, {})
                for percentile in ('median', 'P95', 'P99'):
                    a, b = baseline.get(percentile), values.get(percentile)
                    if not isinstance(a, (int, float)) or not isinstance(b, (int, float)):
                        continue
                    if not math.isfinite(a) or not math.isfinite(b):
                        continue
                    compared += 1
                    # Frame-overrun can be negative. An absolute 1ms allowance avoids a zero denominator.
                    allowance = max(abs(a) * tolerance, 1.0 if 'Ms' in name else 0.0)
                    if b > a + allowance:
                        regressions.append(f"{benchmark['name']} {name}/{percentile}: {a:g} -> {b:g}")
    return compared, regressions


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('baseline')
    parser.add_argument('candidate')
    parser.add_argument('--tolerance', type=float, default=0.15)
    args = parser.parse_args()
    with open(args.baseline) as f:
        before = json.load(f)
    with open(args.candidate) as f:
        after = json.load(f)
    if before.get('context') != after.get('context'):
        sys.exit('Refusing incomparable device/runtime contexts. Repeat both measurements on the same device.')
    count, failures = compare(before, after, args.tolerance)
    if not count:
        sys.exit('No matching numeric benchmark metrics.')
    print(f'Compared {count} metrics; {len(failures)} regressions')
    for failure in failures:
        print(failure)
    return bool(failures)


if __name__ == '__main__':
    sys.exit(main())
