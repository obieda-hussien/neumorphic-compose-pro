#!/usr/bin/env python3
"""Bundle a locally staged Maven repository for Central Portal; no network or credentials needed."""
import hashlib
import pathlib
import sys
import zipfile

root = pathlib.Path(sys.argv[1])
output = pathlib.Path(sys.argv[2])
files = sorted(p for p in root.rglob('*') if p.is_file() and p.suffix not in ('.md5', '.sha1', '.sha256', '.sha512')
               and not p.name.startswith('maven-metadata'))
poms = [p for p in files if p.suffix == '.pom']
if not poms:
    sys.exit('No staged publications')
for pom in poms:
    stem = pom.name[:-4]
    for suffix in ('.aar', '-sources.jar', '-javadoc.jar', '.pom'):
        artifact = pom.with_name(stem + suffix)
        if not artifact.exists() or not artifact.with_name(artifact.name + '.asc').exists():
            sys.exit(f'Missing artifact or signature: {artifact.name}')
output.parent.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as bundle:
    for file in files:
        data = file.read_bytes()
        relative = file.relative_to(root).as_posix()
        bundle.writestr(relative, data)
        if file.suffix != '.asc':
            for algorithm in ('md5', 'sha1', 'sha256', 'sha512'):
                bundle.writestr(relative + '.' + algorithm, hashlib.new(algorithm, data).hexdigest())
print(output)
