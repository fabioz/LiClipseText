/**
 * Copyright (c) 2013-2016 by Brainwy Software Ltda. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license.txt included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package org.brainwy.liclipsetext.editor.common.partitioning;

public class ColorKeys {
	public static final String FOREGROUND = "foreground";
	public static final String BACKGROUND = "background";
	public static final String SELECTION_FOREGROUND = "selectionForeground";
	public static final String SELECTION_BACKGROUND = "selectionBackground";
	public static final String CURRENT_LINE = "currentLine";
	public static final String CURRENT_LINE_IN_WIDGETS = "currentLineInWidgets";
	public static final String LINE_NUMBER = "lineNumber";
	public static final String SEARCH_RESULT_INDICATION = "searchResultIndication";
	public static final String FILTERED_SEARCH_RESULT_INDICATION = "filteredSearchResultIndication";
	public static final String OCCURRENCE_INDICATION = "occurrenceIndication";
	public static final String WRITE_OCCURRENCE_INDICATION = "writeOccurrenceIndication";
	public static final String DELETION_INDICATION = "deletionIndication";
	public static final String FIND_SCOPE = "findScope";
	public static final String SINGLE_LINE_COMMENT = "singleLineComment";
	public static final String MULTI_LINE_COMMENT = "multiLineComment";
	public static final String COMMENT_TASK_TAG = "commentTaskTag";
	public static final String SOURCE_HOVER_BACKGROUND = "sourceHoverBackground";
	public static final String NUMBER = "number";
	public static final String STRING = "string";
	public static final String BRACKET = "bracket";
	public static final String OPERATOR = "operator";
	public static final String KEYWORD = "keyword";
	public static final String CLASS = "class";
	public static final String INTERFACE = "interface";
	public static final String ENUM = "enum";
	public static final String METHOD = "method";
	public static final String METHOD_DECLARATION = "methodDeclaration";
	public static final String ANNOTATION = "annotation";
	public static final String LOCAL_VARIABLE = "localVariable";
	public static final String LOCAL_VARIABLE_DECLARATION = "localVariableDeclaration";
	public static final String INHERITED_METHOD = "inheritedMethod";
	public static final String ABSTRACT_METHOD = "abstractMethod";
	public static final String STATIC_METHOD = "staticMethod";
	public static final String JAVADOC = "javadoc";
	public static final String JAVADOC_TAG = "javadocTag";
	public static final String JAVADOC_KEYWORD = "javadocKeyword";
	public static final String JAVADOC_LINK = "javadocLink";
	public static final String FIELD = "field";
	public static final String STATIC_FIELD = "staticField";
	public static final String STATIC_FINAL_FIELD = "staticFinalField";
	public static final String PARAMETER_VARIABLE = "parameterVariable";
	public static final String TYPE_ARGUMENT = "typeArgument";
	public static final String TYPE_PARAMETER = "typeParameter";
	public static final String DEPRECATED_MEMBER = "deprecatedMember";
	public static final String DEBUG_CURRENT_INSTRUCTION_POINTER = "debugCurrentInstructionPointer";
	public static final String DEBUG_SECONDARY_INSTRUCTION_POINTER = "debugSecondaryInstructionPointer";
	public static final String CONSTANT = "constant";

	// Colors not in the default (so, all of those must have nice defaults).
	public static final String STDERR = "stderr";
	public static final String STDIN = "stdin";
	public static final String STDOUT = "stdout";
	public static final String HYPERLINK = "hyperlink";
	public static final String ACTIVE_HYPERLINK = "activeHyperlink";
	public static final String MATCHING_BRACKET = "matchingBracket";
	public static final String SEARCH_VIEW_MATCH_HIGHLIGHT = "searchViewMatchHighlight";

	// Not in default: compare editor
	public static final String COMPARE_EDITOR_OUTGOING_COLOR = "compareOutgoing";
	public static final String COMPARE_EDITOR_INCOMING_COLOR = "compareIncoming";
	public static final String COMPARE_EDITOR_CONFLICTING_COLOR = "compareConflicting";
	public static final String COMPARE_EDITOR_RESOLVED_COLOR = "compareResolved";

	// Not in default: EGit (VCS = version control system)
	public static final String VCS_UNCOMMITED_CHANGE_FOREGROUND = "vcsUncommitedForeground";
	public static final String VCS_UNCOMMITED_CHANGE_BACKGROUND = "vcsUncommitedBackground";

	public static final String VCS_DIFF_HUNK_FOREGROUND = "vcsDiffHunkForeground";
	public static final String VCS_DIFF_HUNK_BACKGROUND = "vcsDiffHunkBackground";

	public static final String VCS_DIFF_ADD_FOREGROUND = "vcsDiffAddForeground";
	public static final String VCS_DIFF_ADD_BACKGROUND = "vcsDiffAddBackground";

	public static final String VCS_DIFF_REMOVE_FOREGROUND = "vcsDiffRemoveForeground";
	public static final String VCS_DIFF_REMOVE_BACKGROUND = "vcsDiffRemoveBackground";

	public static final String VCS_DIFF_HEADLINE_FOREGROUND = "vcsDiffHeadlineForeground";
	public static final String VCS_DIFF_HEADLINE_BACKGROUND = "vcsDiffHeadlineBackground";

	public static final String VCS_RESOURCE_IGNORED_FOREGROUND = "vcsResourceIgnoredForeground";
	public static final String VCS_RESOURCE_IGNORED_BACKGROUND = "vcsResourceIgnoredBackground";

	public static final String SCROLL_FOREGROUND = "scrollForeground";
	public static final String SCROLL_BACKGROUND = "scrollBackground";

	public static final String SELECTED_TAB_BACKGROUND = "selectedTabBackground";
	public static final String TREE_ARROWS_FOREGROUND = "treeArrowsForeground";
}
