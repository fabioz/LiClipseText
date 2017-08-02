Note that this is an internal copy of the TM4E plugin, found in:

https://github.com/eclipse/tm4e

It includes the org.eclipse.tm4e.core and org.eclipse.tm4e.registry "as is"
(excluding org.eclipse.tm4e.core.internal.css and org.eclipse.tm4e.core.theme.css)
and any modifications should be first done on TM4E and later just replicated here
(note that both plugins are EPL, so, the license shouldn't be a problem).

This is done to avoid having a dependency to tm4e as it brings editors and
definitions which are unwanted (LiClipseText only uses its textmate parsing
functions).