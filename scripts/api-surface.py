#!/usr/bin/env python3
"""Dump public/protected JVM descriptors from an AAR or compare two built AARs.
Usage: api-surface.py current.aar [baseline.aar]
Additions pass; removed signatures fail. DefaultImpls and Kotlin synthetic bridges are included.
"""
import pathlib
import subprocess
import sys
import tempfile
import zipfile


def surface(aar):
    result = set()
    with tempfile.TemporaryDirectory() as directory:
        jar = pathlib.Path(directory) / 'classes.jar'
        with zipfile.ZipFile(aar) as archive:
            jar.write_bytes(archive.read('classes.jar'))
        with zipfile.ZipFile(jar) as archive:
            classes = [n[:-6].replace('/', '.') for n in archive.namelist()
                       if n.endswith('.class') and '/internal/' not in n and not n.startswith(('META-INF/', 'kotlin/'))]
        for name in classes:
            lines = subprocess.check_output(['javap', '-protected', '-s', '-classpath', str(jar), name], text=True).splitlines()
            declaration = next((line for line in lines if line.endswith('{')), '')
            if not declaration.startswith('public '):
                continue
            result.add(name + ' :: ' + declaration)
            previous = ''
            for line in lines:
                line = line.strip()
                if line.startswith(('public ', 'protected ')) and not line.endswith('{'):
                    previous = line
                elif line.startswith('descriptor:') and previous:
                    result.add(name + ' :: ' + previous + ' ' + line)
                    previous = ''
    return result


if __name__ == '__main__':
    current = surface(sys.argv[1])
    if len(sys.argv) > 2:
        missing = surface(sys.argv[2]) - current
        for line in sorted(missing):
            print('REMOVED:', line)
        sys.exit(bool(missing))
    print('\n'.join(sorted(current)))
