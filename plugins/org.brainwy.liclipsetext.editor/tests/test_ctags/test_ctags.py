#coding: utf-8

import subprocess
if __name__ == '__main__':
    p = subprocess.Popen([
        r'X:\liclipse\plugins\com.brainwy.liclipse.editor\libs\ctags.exe', 
#         '--language-force=C++', 
#         '-x', #cross reference
#         '-e', #etags
#         '−−tag−relative=no',
#         '--excmd=number', 
#         '--c-kinds=+defgpstux', 
        '--excmd=number', 
#         '--fields=+iaS',
#         '--fields=-sf',
#         '−−file−scope=no',
#         '--extra=+q', #--works even with etags to generate the class information
#         '--extra=-f',
        '-f', 
        '-', #force stdout 
#         '--verbose=yes',
#         '--language-force=c++',
#         '--stdin-filename=o.cpp',  #Attempt to read from stdin (lives in https://github.com/fabioz/ctags)
#         'o.cpp', 
        r'X:\liclipse\plugins\com.brainwy.liclipse.editor\tests\test_ctags\my.cpp', 
#         r'X:\liclipse\plugins\com.brainwy.liclipse.editor\tests\test_ctags\my.cpp', 
    ],
    stdin=subprocess.PIPE)
    
#     with open(r'X:\liclipse\plugins\com.brainwy.liclipse.editor\tests\test_ctags\my.cpp', 'r') as s:
#         p.stdin.write(s.read())
#         p.stdin.flush()
#         p.stdin.close()
    
    print  p.communicate()
    p.wait()
    
    # etags: gives name, line and offset
    # 
    # X:\liclipse\plugins\com.brainwy.liclipse.editor\tests\test_ctags\my.cpp,209
    # class CRectangle {CRectangle5,65
    #     int x, y;x6,85
    #     int x, y;y6,85
    #     int area () {return (x*y);}area9,143
    # void CRectangle::set_values (int a, int b) {set_values12,182
    # int main () {main17,253
    
    # cross reference
    # CRectangle       class         5 X:\liclipse\plugins\com.brainwy.liclipse.editor\tests\test_ctags\my.cpp class CRectangle {
    # area             function      9 X:\liclipse\plugins\com.brainwy.liclipse.editor\tests\test_ctags\my.cpp int area () {return (x*y);}
    # main             function     17 X:\liclipse\plugins\com.brainwy.liclipse.editor\tests\test_ctags\my.cpp int main () {
    # set_values       function     12 X:\liclipse\plugins\com.brainwy.liclipse.editor\tests\test_ctags\my.cpp void CRectangle::set_values (int a, int b) {
    # x                member        6 X:\liclipse\plugins\com.brainwy.liclipse.editor\tests\test_ctags\my.cpp int x, y;
    # y                member        6 X:\liclipse\plugins\com.brainwy.liclipse.editor\tests\test_ctags\my.cpp int x, y;


    # Default
    # CRectangle    X:\liclipse\plugins\com.brainwy.liclipse.editor\tests\test_ctags\my.cpp    /^class CRectangle {$/;"    c    file:
    # area    X:\liclipse\plugins\com.brainwy.liclipse.editor\tests\test_ctags\my.cpp    /^    int area () {return (x*y);}$/;"    f    class:CRectangle
    # main    X:\liclipse\plugins\com.brainwy.liclipse.editor\tests\test_ctags\my.cpp    /^int main () {$/;"    f
    # set_values    X:\liclipse\plugins\com.brainwy.liclipse.editor\tests\test_ctags\my.cpp    /^void CRectangle::set_values (int a, int b) {$/;"    f    class:CRectangle
    # x    X:\liclipse\plugins\com.brainwy.liclipse.editor\tests\test_ctags\my.cpp    /^    int x, y;$/;"    m    class:CRectangle    file:
    # y    X:\liclipse\plugins\com.brainwy.liclipse.editor\tests\test_ctags\my.cpp    /^    int x, y;$/;"    m    class:CRectangle    file:
