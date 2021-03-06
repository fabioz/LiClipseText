import sys
import os
import re



def find_files(top, additional=()):
    print(top)
    search = ('feature.xml', 'pom.xml', 'manifest.mf') + additional
    for root, dirs, files in os.walk(top):
        for d in ('.svn', '.git', '.metadata'):
            if d in dirs:
                  dirs.remove(d)

        for file in files:
            if file.lower() in search:
                yield os.path.join(root, file)

def update_version(version):
    dirname = os.path.dirname(os.path.abspath(__file__))
    for f in find_files(dirname):
        with open(f, 'r') as stream:
            contents = stream.read()

        new_contents = fix_contents_version(contents, version)
        if contents != new_contents:
            with open(f, 'w') as stream:
                stream.write(new_contents)

def update_version_in_liclipse(version):
    liclipse_dirname = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    liclipse_dir = os.path.join(liclipse_dirname, 'liclipse')
    assert os.path.exists(liclipse_dir)
    for f in find_files(liclipse_dir, ('liclipse.product',)):
        with open(f, 'r') as stream:
            contents = stream.read()

        new_contents = fix_liclipse_contents_version(contents, version)
        if contents != new_contents:
            with open(f, 'w') as stream:
                stream.write(new_contents)


def fix_contents_version(contents, version):
    bugfixversion = int(re.sub(r'^\d\.\d\.(\d)', r'\1', version))
    nextversion = re.sub(r'^(\d\.\d\.)\d', r'\1', version) + str(bugfixversion + 1)
    contents = re.sub(r'(bundle-version=")\[\d\.\d\.\d,\d\.\d\.\d\)"', r'\1[%s,%s)"' %(version, nextversion), contents)
    contents = re.sub(r'(bundle-version=")\[\d\.\d\.\d,\s+\d\.\d\.\d\)"', r'\1[%s,%s)"' %(version, nextversion), contents)
    contents = re.sub(r'(version=)\"\d\.\d\.\d(\.qualifier\")', r'\1"%s\2' % (version,), contents)
    contents = re.sub(r'(<version)>\d\.\d\.\d(-SNAPSHOT</version>)', r'\1>%s\2' % (version,), contents)
    contents = re.sub(r'(Bundle-Version:)\s\d\.\d\.\d(\.qualifier)', r'\1 %s\2' % (version,), contents)

    return contents

def fix_liclipse_contents_version(contents, version):
    bugfixversion = int(re.sub(r'^\d\.\d\.(\d)', r'\1', version))
    nextversion = re.sub(r'^(\d\.\d\.)\d', r'\1', version) + str(bugfixversion + 1)

    contents = re.sub(r'((org)\.brainwy\.liclipsetext(\.\w+)?;)(bundle-version=")\[\d\.\d\.\d,\d\.\d\.\d\)"', r'\1\4[%s,%s)"' % (version, nextversion), contents)
    contents = re.sub(r'(<feature id="org\.brainwy\.liclipsetext\.feature" version=")(\d\.\d\.\d)(\.qualifier"/>)', r'\g<1>%s\3' % (version,), contents)
    return contents


def test_lines():
    '''
    Things we must match:

        version="3.6.0.qualifier"
         <version>3.6.0-SNAPSHOT</version>
         Bundle-Version: 3.6.0.qualifier
         org.brainwy.liclipsetext.shared_core;bundle-version="[3.6.0,3.6.1)",
    '''

    contents = fix_contents_version('''version="3.6.0.qualifier"
     <version>3.6.0-SNAPSHOT</version>
     Bundle-Version: 3.6.0.qualifier
     org.brainwy.liclipsetext.shared_core;bundle-version="[3.6.0,3.6.1)",''', '3.7.1')

    expected = '''version="3.7.1.qualifier"
     <version>3.7.1-SNAPSHOT</version>
     Bundle-Version: 3.7.1.qualifier
     org.brainwy.liclipsetext.shared_core;bundle-version="[3.7.1,3.7.2)",'''
    assert contents.splitlines() == expected.splitlines(), '%s\n!=\n%s' % (contents, expected)
    print('Tests passed')



if __name__ == '__main__':
    if len(sys.argv) == 2:
        if sys.argv[1] == '--test':
            test_lines()
        else:
            update_version(sys.argv[1])
            update_version_in_liclipse(sys.argv[1])
    else:
        print('This script requires the new version (i.e.: 3.6.0)')
