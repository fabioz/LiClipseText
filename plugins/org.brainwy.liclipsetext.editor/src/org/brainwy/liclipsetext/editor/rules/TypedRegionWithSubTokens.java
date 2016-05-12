/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.rules;

import java.util.ArrayList;
import java.util.List;

import org.brainwy.liclipsetext.shared_core.partitioner.DummyToken;
import org.brainwy.liclipsetext.shared_core.partitioner.SubRuleToken;
import org.brainwy.liclipsetext.shared_core.structure.LowMemoryArrayList;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.rules.IToken;

public final class TypedRegionWithSubTokens extends Region implements ITypedRegion {

    private final String fType;

    /**
     * Note: SubRuleToken must be relative to the offset in this region.
     */
    private SubRuleToken fSubRuleToken;

    public TypedRegionWithSubTokens(int offset, int length, String type, SubRuleToken subRuleToken) {
        super(offset, length);
        fType = type;
        fSubRuleToken = subRuleToken;
    }

    public String getType() {
        return fType;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TypedRegionWithSubTokens) {
            TypedRegionWithSubTokens r = (TypedRegionWithSubTokens) o;
            return super.equals(r) && ((fType == null && r.getType() == null) || fType.equals(r.getType()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int type = fType == null ? 0 : fType.hashCode();
        return super.hashCode() | type;
    }

    @Override
    public String toString() {
        return fType + " - " + super.toString(); //$NON-NLS-1$
    }

    public void setSubRuleToken(SubRuleToken subRuleToken) {
        fSubRuleToken = subRuleToken;
    }

    /**
     * Note: the offset should be relative to this region for the returned sub rule tokens.
     */
    public SubRuleToken getSubRuleToken() {
        return fSubRuleToken;
    }

    public List<String> getTypesAtOffset(int offset) {
        offset -= this.getOffset();
        if (offset >= 0) {
            if (this.fSubRuleToken != null) {
                List<IToken> lst = new ArrayList<>();
                this.fSubRuleToken.fillWithTokensAtOffset(offset, lst);
                ArrayList<String> ret = new ArrayList<>(lst.size());
                for (IToken token : lst) {
                    Object data = token.getData();
                    if (data != null && !(token instanceof DummyToken)) {
                        ret.add(data.toString());
                    }
                }
                return ret;
            }
        }
        return new LowMemoryArrayList<>();
    }

}