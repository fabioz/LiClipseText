# Code to generate values:
# from xgui20.xgui.entities import entity_registry
# from pprint import pprint
# from coilib50.basic.xfield import XListOf
# class_to_members = {}
#
# classes = []
# for entity, (module, caption, root) in entity_registry.ENTITIES.iteritems():
#     # print entity, (module, caption, root)
#     import_path = '%s.%s_field.%sField' % (module, entity, caption)
#     klass = entity_registry.ImportClass(import_path)
#     if klass.__name__.endswith('Field'):
#         vals = class_to_members[klass.__name__[:-5]] = []
#
#         for d in dir(klass):
#             if not d.startswith('_') and d[0].islower():
#                 if d == 'sub_fields':
#                     continue
#                 try:
#                     values = getattr(klass, d).values
#                     vals.append((d, values))
#                 except:
#                     vals.append((d, getattr(klass, d).initial_value))
#
# print '{'
# for c, members in sorted(class_to_members.iteritems()):
#     print '%r:%r' % (c, members)
# print '}'


from types import NoneType
from pprint import pprint

class_to_members = \
{
'BoolColumn':[('caption', ''), ('caption_cmd', ''), ('cmd', ''), ('read_only_expr', ''), ('read_only_refresh', ''), ('stretchable', False), ('validation_expr', ''), ('visible_expr', ''), ('width', 75)],
'Boolean':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('caption_position', ('left', 'right')), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('horizontal_size_policy', ('Minimum', 'Fixed')), ('orientation', ('horizontal', 'vertical')), ('refresh_on', ''), ('tool_tip_expr', ''), ('value', False), ('visible_expr', '')],
'CheckGroup':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('caption_refresh', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('enable_refresh', ''), ('expanded_expr', ''), ('expanded_refresh', ''), ('orientation', ('horizontal', 'vertical')), ('tool_tip_expr', ''), ('tool_tip_refresh', ''), ('visible_expr', ''), ('visible_refresh', '')],
'ColorButton':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('normalized', False), ('orientation', ('horizontal', 'vertical')), ('refresh_on', ''), ('tool_tip_expr', ''), ('value', (255, 0, 0)), ('visible_expr', '')],
'ColorColumn':[('caption', ''), ('caption_cmd', ''), ('cmd', ''), ('read_only_expr', ''), ('read_only_refresh', ''), ('stretchable', False), ('validation_expr', ''), ('visible_expr', ''), ('width', 75)],
'Command':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('orientation', ('horizontal', 'vertical')), ('refresh_on', ''), ('tool_tip_expr', ''), ('visible_expr', '')],
'DateTime':[('calendar', False), ('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('max_value_expr', ''), ('min_value_expr', ''), ('orientation', ('horizontal', 'vertical')), ('refresh_on', ''), ('style', ['date', 'datetime', 'time']), ('tool_tip_expr', ''), ('visible_expr', '')],
'DateTimeColumn':[('caption', ''), ('caption_cmd', ''), ('cmd', ''), ('format_expr', ''), ('read_only_expr', ''), ('read_only_refresh', ''), ('stretchable', False), ('style', ['datetime', 'date', 'time']), ('validation_expr', ''), ('visible_expr', ''), ('width', 75)],
'Dialog':[('auto_default', False), ('base_class', 'QDialog'), ('buttons', ['ok']), ('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('class_name', 'MyDialog'), ('cmd', ''), ('controller', ''), ('enable_expr', ''), ('height', 480), ('layout_margin', 5), ('layout_spacing', 5), ('margin_indent', -1), ('margin_width', 110), ('refresh_on', ''), ('show_whats_this', False), ('spacer_resize_policy', ('MinimumExpanding', 'Minimum', 'Maximum')), ('style', ('fixed', 'scrollable')), ('tool_tip_expr', ''), ('top_layout_margin', 5), ('top_layout_spacing', 5), ('visible_expr', ''), ('width', 600)],
'Dict':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('orientation', ('horizontal', 'vertical')), ('refresh_on', ''), ('tool_tip_expr', ''), ('values_cmd', ''), ('visible_expr', '')],
'DynamicTable':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('caption_refresh', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('enable_refresh', ''), ('horizontal_header', ''), ('horizontal_stretchable', False), ('orientation', ('horizontal', 'vertical')), ('refresh_on', ''), ('selection_cmd', ''), ('selection_mode', ('single_cell', 'multi_cell', 'single_row', 'multi_row')), ('tool_tip_expr', ''), ('tool_tip_refresh', ''), ('vertical_header', ''), ('visible_expr', ''), ('visible_refresh', '')],
'Editors':[('caption', ''), ('cmd', ''), ('create_caption', (True, False)), ('create_page_cmd', ''), ('indent', True), ('obtain_data_cmd', ''), ('obtain_editor_id_cmd', ''), ('orientation', ('horizontal', 'vertical')), ('vertical_size_policy', ('Expanding', 'Fixed', 'Preferred'))],
'Filename':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('file_type', ('file', 'directory')), ('file_types_cmd', ''), ('initial_dir_cmd', ''), ('initial_filter_cmd', ''), ('io_mode', ('load', 'save')), ('orientation', ('horizontal', 'vertical')), ('refresh_on', ''), ('tool_tip_expr', ''), ('value', ''), ('visible_expr', '')],
'FixedArray':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('caption_refresh', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('enable_refresh', ''), ('float_format', '%g'), ('max_value_expr', ''), ('min_max_refresh', ''), ('min_value_expr', ''), ('orientation', ('horizontal', 'vertical')), ('should_validate_by_category', True), ('show_unit', True), ('style', ('edit', 'readonly')), ('tool_tip_expr', ''), ('tool_tip_refresh', ''), ('visible_expr', ''), ('visible_refresh', '')],
'Float':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('check_cmd', ''), ('cmd', ''), ('create_caption', (True, False)), ('decimals', 2), ('dimension', 1), ('enable_expr', ''), ('format_expr', '%g'), ('horstreth', 255), ('max_value_expr', ''), ('min_value_expr', ''), ('orientation', ('horizontal', 'vertical')), ('refresh_on', ''), ('single_step', 1.0), ('style', ('edit', 'label', 'spin')), ('tool_tip_expr', ''), ('unit_style', ('none', 'combo', 'label')), ('unit_width', 70), ('visible_expr', '')],
'FloatColumn':[('caption', ''), ('caption_cmd', ''), ('cmd', ''), ('float_format', '%g'), ('read_only_expr', ''), ('read_only_refresh', ''), ('stretchable', False), ('validation_expr', ''), ('visible_expr', ''), ('width', 75)],
'FloatQuantityColumn':[('caption', ''), ('caption_cmd', ''), ('category', ''), ('cmd', ''), ('default_unit', ''), ('float_format', '%g'), ('read_only_expr', ''), ('read_only_refresh', ''), ('show_unit', True), ('stretchable', False), ('unit_row_enabled', True), ('validation_expr', ''), ('visible_expr', ''), ('width', 75)],
'FontButton':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('orientation', ('horizontal', 'vertical')), ('refresh_on', ''), ('tool_tip_expr', ''), ('visible_expr', '')],
'FractionColumn':[('caption', ''), ('caption_cmd', ''), ('category', ''), ('cmd', ''), ('float_format', '%g'), ('only_fraction_part', False), ('read_only_expr', ''), ('read_only_refresh', ''), ('show_unit', True), ('stretchable', False), ('unit_row_enabled', True), ('validation_expr', ''), ('visible_expr', ''), ('width', 75)],
'FractionScalar':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('caption_refresh', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('enable_refresh', ''), ('float_format', '%g'), ('max_value_expr', ''), ('min_max_refresh', ''), ('min_value_expr', ''), ('orientation', ('horizontal', 'vertical')), ('should_validate_by_category', True), ('show_unit', True), ('style', ('edit', 'readonly')), ('tool_tip_expr', ''), ('tool_tip_refresh', ''), ('visible_expr', ''), ('visible_refresh', '')],
'Group':[('add_spacer', False), ('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('checkable', False), ('cmd', ''), ('enable_expr', ''), ('horizontal_size_type', ('Preferred', 'Fixed', 'Expanding')), ('refresh_on', ''), ('spacer_resize_policy', ('Expanding', 'MinimumExpanding', 'Minimum', 'Maximum')), ('style', ('groupbox', 'indent', 'invisible')), ('tool_tip_expr', ''), ('vertical_size_type', ('Fixed', 'Expanding', 'Preferred')), ('visible_expr', '')],
'HorizontalContainer':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('orientation', ('horizontal', 'vertical')), ('refresh_on', ''), ('spacer_policy', ('NoSpacer', 'LeftSpacer', 'RightSpacer')), ('tool_tip_expr', ''), ('use_child_headers', False), ('visible_expr', '')],
'Image':[('alignment', ('AlignLeft', 'AlignRight', 'AlignCenter')), ('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('orientation', ('horizontal', 'vertical')), ('refresh_on', ''), ('tool_tip_expr', ''), ('visible_expr', '')],
'Import':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('data', ''), ('enable_expr', ''), ('filename', ''), ('refresh_on', ''), ('tool_tip_expr', ''), ('visible_expr', '')],
'IntColumn':[('caption', ''), ('caption_cmd', ''), ('cmd', ''), ('float_format', '%g'), ('read_only_expr', ''), ('read_only_refresh', ''), ('stretchable', False), ('style', ('edit', 'spin')), ('validation_expr', ''), ('visible_expr', ''), ('width', 75)],
'Integer':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('check_cmd', ''), ('cmd', ''), ('create_caption', (True, False)), ('dimension', 1), ('enable_expr', ''), ('format_expr', ''), ('max_value_expr', ''), ('min_value_expr', ''), ('one_based_index_expr', 'False'), ('orientation', ('horizontal', 'vertical')), ('read_only_expr', ''), ('refresh_on', ''), ('style', ('edit', 'spin', 'range', 'range_span_and_spin', 'slider_and_spin')), ('tool_tip_expr', ''), ('visible_expr', '')],
'MultiDataChoice':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('enable_expr', ''), ('item_captions', []), ('layout_margin', 30), ('layout_spacing', 60), ('refresh_on', ''), ('spacer_resize_policy', ('MinimumExpanding', 'Minimum')), ('tool_tip_expr', ''), ('visible_expr', '')],
'MultiDataChoiceItem':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('enable_expr', ''), ('key', ''), ('layout_margin', 0), ('layout_spacing', 0), ('refresh_on', ''), ('spacer_resize_policy', ('MinimumExpanding', 'Minimum')), ('tool_tip_expr', ''), ('visible_expr', '')],
'Page':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('enable_expr', ''), ('layout_margin', 5), ('layout_spacing', 5), ('refresh_on', ''), ('spacer_resize_policy', ('MinimumExpanding', 'Minimum', 'Maximum', 'NoSpacer')), ('tool_tip_expr', ''), ('value', None), ('visible_expr', '')],
'Pages':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('enable_expr', ''), ('layout_margin', 5), ('left_margin', 15), ('refresh_on', ''), ('spacer_resize_policy', ('MinimumExpanding', 'Minimum', 'Maximum')), ('tool_tip_expr', ''), ('visible_expr', '')],
'Scalar':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('caption_refresh', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('enable_refresh', ''), ('float_format', '%g'), ('max_value_expr', ''), ('min_max_refresh', ''), ('min_value_expr', ''), ('orientation', ('horizontal', 'vertical')), ('should_validate_by_category', True), ('show_unit', True), ('style', ('edit', 'readonly')), ('tool_tip_expr', ''), ('tool_tip_refresh', ''), ('visible_expr', ''), ('visible_refresh', '')],
'ScalarColumn':[('caption', ''), ('caption_cmd', ''), ('category', ''), ('cmd', ''), ('float_format', '%g'), ('read_only_expr', ''), ('read_only_refresh', ''), ('show_unit', True), ('stretchable', False), ('unit_row_enabled', True), ('validation_expr', ''), ('visible_expr', ''), ('width', 75)],
'Select1':[('auto_fit', False), ('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('orientation', ('horizontal', 'vertical')), ('refresh_on', ''), ('searchable', True), ('size_adjust_policy', ('ajust_to_contents_on_first_view', 'ajust_to_contents', 'ajust_to_minimum_contents_length', 'ajust_to_minimum_contents_length_with_icon')), ('style', ('combo_box', 'list_box', 'check_combo_box', 'radio_buttons')), ('tool_tip_expr', ''), ('values', []), ('values_cmd', ''), ('visible_count', 0), ('visible_expr', '')],
'Select1Column':[('caption', ''), ('caption_cmd', ''), ('cmd', ''), ('read_only_expr', ''), ('read_only_refresh', ''), ('stretchable', False), ('validation_expr', ''), ('values_cmd', ''), ('visible_expr', ''), ('width', 75)],
'SelectN':[('auto_fit', False), ('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('orientation', ('horizontal', 'vertical')), ('refresh_on', ''), ('style', ('check_list', 'multi_selection', 'check_list_with_toolbar', 'two_lists')), ('tool_tip_expr', ''), ('values', []), ('values_cmd', ''), ('visible_count', 0), ('visible_expr', '')],
'Splitter':[],
'SplitterPage':[('enable_expr', ''), ('visible_expr', ''), ('visible_refresh', '')],
'StringColumn':[('caption', ''), ('caption_cmd', ''), ('cmd', ''), ('read_only_expr', ''), ('read_only_refresh', ''), ('stretchable', False), ('validation_expr', ''), ('visible_expr', ''), ('width', 75)],
'SubjectTable':[('alternate_row_colors', False), ('background_color_expr', ''), ('background_color_refresh', ''), ('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('caption_refresh', ''), ('cmd', ''), ('create_caption', (True, False)), ('editing_status_cmd', ''), ('enable_expr', ''), ('enable_refresh', ''), ('hidden_subject_indexes_cmd', ''), ('hidden_subject_indexes_refresh', ''), ('orientation', ('horizontal', 'vertical')), ('resize_columns_automatically', True), ('selected_cell_cmd', ''), ('selection_cmd', ''), ('selection_mode', ('single_cell', 'multi_cell', 'single_row', 'multi_row')), ('should_trigger_modify_while_import', False), ('show_vertical_header', False), ('sorting_enabled', False), ('tool_tip_expr', ''), ('tool_tip_refresh', ''), ('visible_expr', ''), ('visible_refresh', '')],
'Tab':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('caption_refresh', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('enable_refresh', ''), ('orientation', ('horizontal', 'vertical')), ('spacer_resize_policy', ('MinimumExpanding', 'Minimum', 'Maximum')), ('tool_tip_expr', ''), ('tool_tip_refresh', ''), ('visible_expr', ''), ('visible_refresh', '')],
'Table':[('alternate_row_colors', False), ('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('double_clicked_cmd', ''), ('enable_expr', ''), ('entity_orientation', ('vertical', 'horizontal')), ('refresh_on', ''), ('selected_cell_cmd', ''), ('selection_mode', ('single', 'multi', 'noselection', 'single_row', 'multi_row')), ('show_vertical_header', False), ('tool_tip_expr', ''), ('vertical_header', ''), ('visible_expr', '')],
'TableColumn':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('enable_expr', ''), ('read_only_expr', ''), ('refresh_on', ''), ('stretchable', False), ('text_alignment', []), ('tool_tip_expr', ''), ('value_type', ('str', 'int', 'float', 'bool', '"select1"')), ('values', []), ('visible_expr', ''), ('width', 75)],
'TableToolBar':[('enable_expr', ''), ('enable_refresh', ''), ('show_buttons', None), ('visible_expr', ''), ('visible_refresh', '')],
'TableView':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('orientation', ('horizontal', 'vertical')), ('refresh_on', ''), ('tool_tip_expr', ''), ('visible_expr', '')],
'Tabs':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('create_page_cmd', ''), ('enable_expr', ''), ('refresh_on', ''), ('tool_tip_expr', ''), ('values', []), ('values_cmd', ''), ('visible_expr', '')],
'Text':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('format_expr', ''), ('orientation', ('horizontal', 'vertical')), ('read_only_expr', ''), ('refresh_on', ''), ('style', ('edit', 'label', 'passwd', 'multiline', 'python-code', 'direct-python-code', 'direct-multiline', 'html')), ('tool_tip_expr', ''), ('validation_callback', None), ('value', ''), ('visible_expr', '')],
'ToolBar':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('enable_expr', ''), ('frame_shape', ('StyledPanel', 'NoFrame')), ('refresh_on', ''), ('spacer_resize_policy', ('MinimumExpanding', 'Minimum', 'Maximum')), ('tool_tip_expr', ''), ('visible_expr', '')],
'ToolButton':[('align', ('left', 'right')), ('auto_raise', False), ('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('checkable', False), ('cmd', ''), ('enable_expr', ''), ('height', 20), ('img', ''), ('refresh_on', ''), ('tool_tip_expr', ''), ('visible_expr', ''), ('whats_this', ''), ('width', 20)],
'Tree':[('caption', ''), ('captions_cmd', ''), ('captions_refresh', ''), ('cmd', ''), ('columns_hidden_cmd', ''), ('columns_hidden_refresh', ''), ('context_menu_callback', ''), ('create_caption', (True, False)), ('double_click_cmd', ''), ('enable_expr', ''), ('enable_refresh', ''), ('keep_state', False), ('orientation', ('horizontal', 'vertical')), ('root_is_decorated', False), ('selection_mode', ('single', 'multiple')), ('tooltip_callback', ''), ('values_cmd', ''), ('values_refresh', ''), ('values_sorted', False), ('visible_expr', ''), ('visible_refresh', '')],
'TreeView':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('orientation', ('horizontal', 'vertical')), ('refresh_on', ''), ('tool_tip_expr', ''), ('visible_expr', '')],
'TreeWidget':[('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('cmd', ''), ('create_caption', (True, False)), ('enable_expr', ''), ('orientation', ('horizontal', 'vertical')), ('refresh_on', ''), ('tool_tip_expr', ''), ('visible_expr', '')],
'TupleTable':[('alternate_row_colors', False), ('background_color_expr', ''), ('background_color_refresh', ''), ('caption', ''), ('caption_cmd', ''), ('caption_color_expr', ''), ('caption_refresh', ''), ('cmd', ''), ('create_caption', (True, False)), ('data_refresh', ''), ('editing_status_cmd', ''), ('enable_expr', ''), ('enable_refresh', ''), ('hidden_subject_indexes_cmd', ''), ('hidden_subject_indexes_refresh', ''), ('orientation', ('horizontal', 'vertical')), ('resize_columns_automatically', True), ('selected_cell_cmd', ''), ('selection_cmd', ''), ('selection_mode', ('single_cell', 'multi_cell', 'single_row', 'multi_row')), ('should_trigger_modify_while_import', False), ('show_vertical_header', False), ('sorting_enabled', False), ('tool_tip_expr', ''), ('tool_tip_refresh', ''), ('visible_expr', ''), ('visible_refresh', '')],
'Widget':[('base_class', 'QWidget'), ('caption', ''), ('class_name', 'MyWidget'), ('controller', ''), ('height', None), ('is_main', False), ('layout_margin', 5), ('layout_spacing', 0), ('margin_indent', -1), ('margin_width', 110), ('show_whats_this', False), ('spacer_resize_policy', ('MinimumExpanding', 'Minimum', 'Maximum')), ('style', ('fixed', 'scrollable')), ('top_layout_margin', 5), ('top_layout_spacing', 0), ('use_apply_on_request', False), ('width', None)]
}

template_variables = {
  'bool' : ['True', 'False'],
}

template = '''
- name: '.%(attr)s'
  description: "= %(default)s%(desc_part2)s"
  pattern: ".%(attr)s = %(default)s"
  icon: attribute
  match_previous_sub_scope: ['default.keyword', '%(parent)s']
'''

template_class = '''
- name: '%(class)s'
  description: ""
  pattern: "%(class)s"
  icon: class
'''

for class_, members in sorted(class_to_members.iteritems()):
    print template_class % {'class':class_}

    for member_type, defaults in sorted(members):
        desc_part2 = ''
        if isinstance(defaults, bool):
            default = '${bool}'
            desc_part2 = ' (default:%s)' % (defaults,)

        elif isinstance(defaults, (str, int, float)):
            default = str(defaults)
            if default:
                default = repr(default)

        elif isinstance(defaults, NoneType):
            default = ''

        elif isinstance(defaults, (list, tuple)):
            new_val = [repr(str(x)) for x in defaults]
            key = '%s' % (member_type,)
            if new_val:
                desc_part2 = ' (default:%s)' % (new_val[0],)

            if member_type in template_variables:
                curr = template_variables[key]
                if curr != new_val:
                    key = '%s_%s' % (class_, member_type)  #create a custom key for this type!

            template_variables[key] = new_val
            default = '${%s}' % key

        else:
            raise AssertionError('Type not handled: ' + str(defaults))

        print template % dict(attr=member_type, parent=class_, default=default, desc_part2=desc_part2)

pprint(template_variables)
