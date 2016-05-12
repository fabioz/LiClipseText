package org.brainwy.liclipsetext.editor.languages.auto_edit;

import java.util.Map;

public abstract class AbstractScopedAutoEditRule implements ILanguageAutoEditRule {

    protected String scope;

    public AbstractScopedAutoEditRule(Map map) {
        setScope(map.remove("scope"));
    }

    private void setScope(Object scope) {
        this.scope = (String) scope;
    }

    public String getScope() {
        return scope;
    }

}
