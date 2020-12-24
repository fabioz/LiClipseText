import os
import sys
parent_dir = os.path.split(__file__)[0]

#=======================================================================================================================
# RunCog
#=======================================================================================================================
def RunCog():

    import cog
    # Encoding needed for html.liclipse
    cog.ENCODING = 'latin1'

    lst = [
        dict(ext='html,htm', kind='html', img='html'),
        dict(ext='xml,xsd', kind='xml', img='xml', filenames=".pydevproject,.project,.classpath", base_type="org.eclipse.core.runtime.xml"),
        dict(ext='js', kind='javascript', img='javascript'),
        dict(ext='ts', kind='source.ts', img='typescript'),
        dict(ext='json', kind='json', img='json'),
        dict(ext='java', kind='java', img='java'),
        dict(ext='liclipse', kind='liclipse', img='liclipse'),
        dict(ext='dxl', kind='dxl', img='dxl'),
        dict(ext='djhtml', kind='django', img='django'),
        dict(ext='py,pyw', kind='python', img='python', filenames="SConstruct,Sconstruct,sconstruct,SConscript"),
        dict(ext='c,cpp,h,hpp', kind='cpp', img='cpp'),
        dict(ext='css', kind='source.css', img='css'),
        dict(ext='scss', kind='source.scss', img='scss'),
        dict(ext='coffee', kind='coffeescript', img='coffee'),
        dict(ext='rst', kind='restructured text', img='rst'),
        dict(ext='dart', kind='dart', img='dart'),
        dict(ext='mako', kind='mako', img='mako'),
        dict(ext='xgui2,xgui2_,xinc', kind='xgui20', img='liclipse'),
        dict(ext='kv', kind='kivy', img='kivy'),
        dict(ext='jl', kind='julia', img='julia'),
        dict(ext='st,stg', kind='stringtemplate', img='stringtemplate'),
        dict(ext='yml,yaml,liclipseprefs', kind='yaml', img='yaml'),
        dict(ext='nim', kind='nim', img='nim'),
        dict(ext='go', kind='go', img='go'),
        dict(ext='jinja2', kind='jinja2', img='jinja2'),
        dict(ext='rb,rbx,Rakefile,rake', kind='source.ruby', img='source.ruby'),
        dict(ext='erb,rhtml,html.erb', kind='text.html.erb', img='text.html.erb'),
        dict(ext='php,php3,php4,php5,phpt,phtml,aw,ctp', kind='text.html.php', img='text.html.php'),
        dict(ext='md,mdown,markdown,markdn', kind='text.html.markdown', img='text.html.markdown'),
        dict(ext='swift', kind='source.swift', img='source.swift'),
        dict(ext='pl,pm,pod,t,PL,psgi', kind='source.perl', img='source.perl'),
        dict(ext='p6,pl6,pm6,nqp', kind='source.perl.6', img='source.perl.6'),
        dict(ext='bat', kind='source.dosbatch', img='source.dosbatch'),
        dict(ext='sh,bash,zsh,bashrc,bash_profile,bash_login,profile,bash_logout,textmate_init', kind='source.shell', img='source.shell'),
        dict(ext='raml', kind='source.raml', img='source.raml'),
        dict(ext='cmake', kind='source.cmake', img='source.cmake', filenames="CMakeLists.txt,cmake"),
        dict(ext='cmakecache', kind='source.cache.cmake', img='source.cache.cmake', filenames="CMakeCache.txt"),
        dict(ext='robot,resource', kind='text.robot', img='text.robot'),
        ]

    plugin_file = os.path.join(parent_dir, 'plugin.xml')
    new_lines = []
    found = False
    with open(plugin_file, 'r') as f:
        for line in f.readlines():
            if line.strip().startswith('EDITORS_LST = '):
                found = True
                line = '    EDITORS_LST = %s\n' % (lst,)
            new_lines.append(line)
    assert found
    with open(plugin_file, 'w') as f:
        f.write(''.join(new_lines))

    cog.RunCogInFiles([plugin_file])
    cog.RunCogInFiles([
        os.path.join(parent_dir, 'src', 'org', 'brainwy', 'liclipsetext', 'editor', 'common',
            'partitioning', 'rules', 'RulesFactory.java')])
    cog.RunCogInFiles([os.path.join(parent_dir, 'languages', 'html.liclipse')])
    cog.RunCogInFiles([os.path.join(parent_dir, 'languages', 'dart.liclipse')])


    base_compare = os.path.join(parent_dir, 'src', 'org', 'brainwy', 'liclipsetext', 'editor', 'compare')


    for x in lst:
        x['kind_upper'] = x['kind'].title().replace(' ', '').replace('.', '')

        template = '''// Automatically Generated in install.py
package org.brainwy.liclipsetext.editor.compare;

public class %(kind_upper)sContentViewerCreator extends AbstractContentViewerCreator {

    public %(kind_upper)sContentViewerCreator() {
        super("%(kind)s");
    }
}'''
        filename = '%sContentViewerCreator.java' % x['kind_upper']

        with open(os.path.join(base_compare, filename), 'w') as f:
            f.write(template % x)

        template = '''// Automatically Generated in install.py
package org.brainwy.liclipsetext.editor.compare;

public class %(kind_upper)sMergeViewerCreator extends AbstractMergeViewerCreator {

    public %(kind_upper)sMergeViewerCreator() {
        super("%(kind)s");
    }
}
'''
        filename = '%sMergeViewerCreator.java' % x['kind_upper']

        with open(os.path.join(base_compare, filename), 'w') as f:
            f.write(template % x)




#=======================================================================================================================
# main
#=======================================================================================================================
if __name__ == '__main__':
    RunCog()

