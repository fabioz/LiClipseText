package org.brainwy.liclipsetext.editor.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.brainwy.liclipsetext.editor.LiClipseTextEditorPlugin;
import org.brainwy.liclipsetext.editor.common.partitioning.IColorCacheProvider;
import org.brainwy.liclipsetext.shared_core.log.Log;
import org.brainwy.liclipsetext.shared_core.string.StringUtils;
import org.brainwy.liclipsetext.shared_core.structure.Tuple3;
import org.brainwy.liclipsetext.shared_core.utils.BaseExtensionHelper;
import org.brainwy.liclipsetext.shared_ui.field_editors.MultiStringFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class LiClipseColorsPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public LiClipseColorsPreferencesPage() {
		super(GRID);
		setPreferenceStore(LiClipseTextEditorPlugin.getDefault().getPreferenceStore());
	}

	@Override
	public void init(IWorkbench workbench) {

	}
	
	@Override
	public void addField(FieldEditor editor) {
		super.addField(editor);
	}

	@Override
	protected void createFieldEditors() {
		Composite p = getFieldEditorParent();
		
        try {
        	IColorCacheProvider provider = (IColorCacheProvider) BaseExtensionHelper.getParticipant(
        			"org.brainwy.liclipsetext.editor.liclipse_color_cache_provider", false);
        	if(provider != null){
        		if(provider.createFieldEditors(this, p)){
        			return;
        		}
        	}
		} catch (Exception e) {
			Log.log(e);
		}


		addField(new MultiStringFieldEditor("LICLIPSE_COLORS",
				"Colors for LiClipse Editors in the format:\n\nname=r,g,b;ITALIC,BOLD,UNDERLINE,STRIKETHROUGH", p, true) {
			@Override
			protected void doStore() {
				String txt = getTextControl().getText();
				Map<String, String> convertToMap = convertToMap(txt);
				IPreferenceStore preferenceStore = getPreferenceStore();
				for (Object[] objects : LiClipseTextPreferences.NAME_COLOR_AND_STYLE) {
					String name = objects[0].toString();
					String converted = convertToMap.get(name);
					if (converted != null) {
						int i = converted.indexOf(';');
						String color = converted;
						String style = "";
						if (i >= 0) {
							color = converted.substring(0, i).trim();
							style = converted.substring(i + 1).trim();
						}
						try {
							Tuple3<Integer, Integer, Integer> convertColor = convertColor(color);
							if (convertColor != null) {
								preferenceStore.setValue(name + "_COLOR",
										StringUtils.join(",", convertColor.o1, convertColor.o2, convertColor.o3));
							}
						} catch (Exception e) {
							Log.log(e);
						}
						try {
							int styleInt = 0;
							if (style.length() > 0) {
								for (String s : StringUtils.split(style, ',')) {
									switch (s) {
									case "BOLD":
										styleInt |= LiClipseTextPreferences.BOLD;
										break;

									case "ITALIC":
										styleInt |= LiClipseTextPreferences.ITALIC;
										break;

									case "UNDERLINE":
										styleInt |= LiClipseTextPreferences.UNDERLINE;
										break;

									case "STRIKETHROUGH":
										styleInt |= LiClipseTextPreferences.STRIKETHROUGH;
										break;
									}
								}
							}
							preferenceStore.setValue(name + "_STYLE", String.valueOf(styleInt));
						} catch (Exception e) {
							Log.log(e);
						}
					}
				}
				preferenceStore.setValue(getPreferenceName(), txt);
			}

			@Override
			protected void doLoad() {
				String finalStr = createStr(false);
				setStringValue(finalStr);
			}

			private String createStr(boolean makeDefault) {
				IPreferenceStore preferenceStore = getPreferenceStore();
				List<String> lst = new ArrayList<>(LiClipseTextPreferences.NAME_COLOR_AND_STYLE.size());
				for (Object[] objects : LiClipseTextPreferences.NAME_COLOR_AND_STYLE) {
					String name = objects[0].toString();
					String colorValue;
					int styleValue;
					if (makeDefault) {
						colorValue = preferenceStore.getDefaultString(name + "_COLOR");
						styleValue = preferenceStore.getDefaultInt(name + "_STYLE");

					} else {
						colorValue = preferenceStore.getString(name + "_COLOR");
						styleValue = preferenceStore.getInt(name + "_STYLE");

					}
					String s = name + "=" + colorValue;
					List<String> stylesLst = new ArrayList<>(4);
					if (styleValue != 0) {
						if ((styleValue & LiClipseTextPreferences.BOLD) != 0) {
							stylesLst.add("BOLD");
						}
						if ((styleValue & LiClipseTextPreferences.ITALIC) != 0) {
							stylesLst.add("ITALIC");
						}
						if ((styleValue & LiClipseTextPreferences.UNDERLINE) != 0) {
							stylesLst.add("UNDERLINE");
						}
						if ((styleValue & LiClipseTextPreferences.STRIKETHROUGH) != 0) {
							stylesLst.add("STRIKETHROUGH");
						}
					}
					if (stylesLst.size() > 0) {
						s += ";" + StringUtils.join(",", stylesLst);
					}
					lst.add(s);
				}
				String finalStr = StringUtils.join("\n", lst);
				return finalStr;
			}

			@Override
			protected void doLoadDefault() {
				String finalStr = createStr(true);
				setStringValue(finalStr);
			}
		});
	}

	public static Map<String, String> convertToMap(String contents) {
		Map<String, String> attributes = new HashMap<>();
		StringTokenizer tokenizer = new StringTokenizer(contents, "\r\n");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			int i = token.indexOf('=');
			if (i <= 0) {
				continue;
			}
			String key = token.substring(0, i).trim();
			String val = token.substring(i + 1).trim();
			attributes.put(key, val);
		}
		return attributes;
	}

	public static Tuple3<Integer, Integer, Integer> convertColor(String value) {
		if (value != null) {
			int r, g, b;
			if (value.startsWith("#")) {
				r = Integer.parseInt(value.substring(1, 3), 16);
				g = Integer.parseInt(value.substring(3, 5), 16);
				b = Integer.parseInt(value.substring(5, 7), 16);

				if (r < 0) {
					r = 0;
				}
				if (g < 0) {
					g = 0;
				}
				if (b < 0) {
					b = 0;
				}
				if (r > 255) {
					r = 255;
				}
				if (g > 255) {
					g = 255;
				}
				if (b > 255) {
					b = 255;
				}
			} else {
				// Not in hexa: i.e.: r,g,b comma-separated.
				String[] s = value.split("\\,");
				if (s.length == 3) {
					r = Integer.parseInt(s[0]);
					g = Integer.parseInt(s[1]);
					b = Integer.parseInt(s[2]);

				} else {
					System.err.println("Unable to recognize: " + value);
					r = 0;
					g = 0;
					b = 0;
				}
			}
			return new Tuple3<Integer, Integer, Integer>(r, g, b);
		} else {
			return null;
		}
	}

}
