LiClipse supports using TextMate bundles to provide highlighting for grammars.

To add any bundle, download it as a zip, rename to .tmbundle and put it in this folder.

For instance, if you go to a bundle located in github, you could download it through the
download button.

The existing ones are created with the commands below (note: -L is to follow redirects):

curl -L -o ruby.tmbundle https://github.com/textmate/ruby.tmbundle/archive/master.zip
curl -L -o textmate.tmbundle https://github.com/textmate/textmate.tmbundle/archive/master.zip
curl -L -o html.tmbundle https://github.com/textmate/html.tmbundle/archive/master.zip
curl -L -o markdown.tmbundle https://github.com/textmate/markdown.tmbundle/archive/master.zip
curl -L -o php.tmbundle https://github.com/textmate/php.tmbundle/archive/master.zip
curl -L -o swift.tmbundle https://github.com/textmate/swift.tmbundle/archive/master.zip
curl -L -o perl.tmbundle https://github.com/textmate/perl.tmbundle/archive/master.zip
curl -L -o shellscript.tmbundle https://github.com/textmate/shellscript.tmbundle/archive/master.zip
curl -L -o raml.tmbundle https://github.com/mulesoft/raml-sublime-plugin/archive/master.zip
curl -L -o css.tmbundle https://github.com/textmate/css.tmbundle/archive/master.zip
curl -L -o scss.tmbundle https://github.com/MarioRicalde/SCSS.tmbundle/archive/master.zip
curl -L -o cmake.tmbundle https://github.com/textmate/cmake.tmbundle/archive/master.zip
curl -L -o javascript.tmbundle https://github.com/textmate/javascript.tmbundle/archive/master.zip

LiClipse should then automatically recognize the bundle.

/Syntaxes
	.tmLanguage files
		Many of the settings in .liclipse have a relation to things defined in the .tmLanguage file.
		However, each bundle has many .tmLanguage files, so, the relation isn't completely direct
		(a single .liclipse file is closer to a whole TM bundle -- if that bundle had only a single
		.tmLanguage file and only .tmPreferences/.tmSnippet related to that language).

/Preferences
	.tmPreferences files
		These define things which we also define in .liclipse files (such as how to add a comment, what's
		a symbol, etc).

/Snippets
	.tmSnippet files
		These are very close to .liclipse templates.

Note that currently the other information on a bundle is *currently* ignored (i.e.: /Commands, /Macros, /Support, etc).