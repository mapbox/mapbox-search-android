import json
import time
import os.path
import argparse

NEED_TESTS = True
SAVE_FILES = True
OVERRIDE_FILES = True

CONFIG_FILE_NAME = 'config.json'
TAB = '    '

TEST_IMPORTS = [
    "com.mapbox.search.dsl.TestCase",
    "com.mapbox.search.dsl.CheckPublicApi",
    "org.junit.jupiter.api.Assertions.assertEquals",
    "org.junit.jupiter.api.TestFactory"
]


def read_config_file():
    try:
        return json.load(open(CONFIG_FILE_NAME))
    except Exception, e:
        raise e


def check_file_exits(file_path):
    return os.path.isfile(file_path)


def save_file(file_path, file_data):
    if not SAVE_FILES:
        return
    if check_file_exits(file_path):
        if not OVERRIDE_FILES:
            raise Exception('Can\'t override file %s' % file_path)

    file = open(file_path, "w")
    file.write(file_data)
    file.close()


def generate_class_package(class_package):
    return "package %s\n\n" % class_package


def generate_import(class_imports):
    if len(class_imports) == 0:
        return ""
    res = ""
    class_imports.sort()
    for class_import in class_imports:
        res += "import %s\n" % class_import
    return res + "\n"


def generate_class_name(class_name):
    return "class %s(\n" % class_name


def generate_default_uppercase_name(field):
    upper_name = field['defaultUpperName'] if 'defaultUpperName' in field else field['name'].upper()
    return "DEFAULT_%s" % upper_name


def is_required(field):
    return 'required' in field


def generate_companion_object(fields):
    res = "%scompanion object {\n" % (TAB)
    for field in fields:
        if not is_required(field):
            res += "%sprivate %sval %s = %s\n" % (TAB * 2,
                                              "const " if 'companionConst' in field and field['companionConst'] else "",
                                              generate_default_uppercase_name(field),
                                              field['defaultValue'])
    res += "%s}\n\n" % TAB
    return res


def generate_class_fields(fields):
    res = ""
    for field in fields:
        res += "%sval %s: %s,\n" % (TAB, field['name'], field['type'])
    return res[:-2] + "\n) {\n\n"


def generate_class_constructor(fields):
    res = TAB + "private constructor(builder: Builder) : this(\n"
    for field in fields:
        res += "%s%s = builder.%s,\n" % (TAB * 2, field['name'], field['name'])
    return res[:-2] + "\n%s)\n\n" % TAB


def generate_builder_constructor(fields):
    res = ""
    for field in fields:
        if is_required(field):
            res +="%sval %s: %s\n" %(TAB *2, field['name'], field['type'])
    if len(res)>0:
        return "(\n"+res+"%s) " % (TAB)
    else:
        return " "

def generate_builder_fields(fields):
    res = TAB + "class Builder%s{\n" % generate_builder_constructor(fields)
    for field in fields:
        if not is_required(field):
            res += "%svar %s: %s = %s\n" % (TAB * 2, field['name'], field['type'],
                                            generate_default_uppercase_name(field))
            res += "%sprivate set\n\n" % (TAB * 3)
    return res


def generate_builder_methods(fields):
    res = ""
    for field in fields:
        if not is_required(field):
            if 'customApply' in field:
                res += "%s%s\n\n" % (TAB * 2, field['customApply'])
            else:
                if 'varargType' in field:
                    apply_params = "vararg " + field['name']
                    apply_setter = field['name']+".toList()"
                    apply_type = field['varargType']
                else:
                    apply_params = field['name']
                    apply_setter = field['customSetter'] if 'customSetter' in field else field['name']
                    apply_type = field['type']
                res += "%sfun %s(%s: %s) = apply { this.%s = %s }\n\n" % (
                    TAB * 2, field['name'], apply_params, apply_type.replace('?',''), field['name'], apply_setter)
    return res


def generate_builder_method(class_name, class_fields):
    return "%sfun build() = %s(this)\n%s}\n}\n" % (TAB * 2, class_name, TAB)


def generate_test_import(class_test_imports):
    _imports = []
    _imports.extend(TEST_IMPORTS)
    _imports.extend(class_test_imports)

    _imports.sort()
    res = ""
    for class_test_import in _imports:
        res += "import %s\n" % class_test_import
    return res + "\n"


def generate_test_name(class_name):
    return "class %sTest {\n\n" % class_name


def generate_public_class_api_test(class_name, fields):
    res = TAB + "@TestFactory\n"
    res += TAB + "fun `Check %s public methods`() = CheckPublicApi {\n" % class_name
    res += "%scheckClass(%s::class) {\n" % (TAB * 2, class_name)
    res += "%spublic {\n" % (TAB * 3)
    res += "%sfields(\n" % (TAB * 4)
    for field in fields:
        res += "%s\"%s\",\n" % (TAB * 5, field['name'])
    res = res[:-2] + "\n"
    res += "%s)\n%s}\n%s}\n%s}\n\n" % (TAB * 4, TAB * 3, TAB * 2, TAB)
    return res


def generate_public_builder_api_test(class_name, fields):
    res = TAB + "@TestFactory\n"
    res += TAB + "fun `Check %s_Builder public methods`() = CheckPublicApi {\n" % class_name
    res += "%scheckClass(%s.Builder::class) {\n" % (TAB * 2, class_name)
    res += "%spublic {\n" % (TAB * 3)
    res += "%sfields(\n" % (TAB * 4)
    for field in fields:
        res += "%s\"%s\",\n" % (TAB * 5, field['name'])
    res = res[:-2] + "\n"
    res += "%s)\n" % (TAB * 4)

    for field in fields:
        if not is_required(field):
            res += "%smethod {\n" % (TAB * 4)
            res += "%sname = \"%s\"\n" % (TAB * 5, field['name'])
            type = field['type'].replace('List', 'Array') if 'varargType' in field else field['type']
            type = type.replace('?', '')
            res += "%sparams = listOf(\"%s\")\n" % (TAB * 5, type.replace('?', ''))
            res += "%sreturnType = \"%s_Builder\"\n" % (TAB * 5, class_name)
            res += "%s}\n" % (TAB * 4)

    res += "%smethod {\n" % (TAB * 4)
    res += "%sname = \"build\"\n" % (TAB * 5)
    res += "%sreturnType = \"%s\"\n" % (TAB * 5, class_name)
    res += "%s}\n" % (TAB * 4)

    res += "%s}\n%s}\n%s}\n\n" % (TAB * 3, TAB * 2, TAB)
    return res


def add_required_field(fields, current_field_name):
    res = ""
    for field in fields:
        if 'required' in field and field['name']!=current_field_name:
            res+="%s = %s" % (field['name'], get_first_field_actual_value_except_default(field))
    return res


def generate_default_test(class_name, fields):
    class_name_lower = class_name[0].lower() + class_name[1:]
    res = TAB + "@TestFactory\n"
    res += TAB + "fun `Check %s default builder`() = TestCase {\n" % class_name
    res += "%sGiven(\"%s with default params\") {\n" % (TAB * 2, class_name)
    res += "%sval %s = %s.Builder(%s).build()\n" % (TAB * 3, class_name_lower, class_name, add_required_field(fields,""))

    for field in fields:
        res += "\n%sWhen(\"Get default %s\") {\n" % (TAB * 3, field['name'])
        res += "%sval actualValue = %s.%s\n" % (TAB * 4, class_name_lower, field['name'])
        # expected_field_value = get_first_field_actual_value_except_default(field) if 'required' in field else field['defaultValue']
        expected_field_value = get_first_field_expected_value(field) if 'required' in field else field['defaultValue']
        # get_first_field_expected_value
        res += "%sThen(\"It should be <%s>\") {\n" % (TAB * 4, expected_field_value.replace("\"","\\\""))
        res += "%sassertEquals(%s, actualValue)\n" % (TAB * 5, expected_field_value)
        res += "%s}\n" % (TAB * 4)
        res += "%s}\n" % (TAB * 3)

    res += "%s}\n%s}\n\n" % (TAB * 2, TAB)

    return res


def get_first_field_actual_value_except_default(field):
    field_default_value = field['defaultValue']
    all_values = [field['actual'] for field in field['testValues']]
    if field_default_value in all_values:
        all_values.remove(field_default_value)
    if len(all_values) == 0:
        raise Exception("Can't find any value except default for %s" % field['name'])
    return all_values[0]


def get_first_field_expected_value(field):
    field_default_value = field['defaultValue']
    all_expected_values = [item['expected'] for item in field['testValues']]
    all_actual_values = [item['actual'] for item in field['testValues']]

    if field_default_value in all_actual_values:
        index = all_actual_values.index(field_default_value)
        del all_actual_values[index]
        del all_expected_values[index]

    if len(all_expected_values) == 0:
        raise Exception("Can't find any value except default for %s" % field['name'])
    return all_expected_values[0]


def generate_custom_test(class_name, fields):
    class_name_lower = class_name[0].lower() + class_name[1:]

    res = TAB + "@TestFactory\n"
    res += TAB + "fun `Check %s custom builder`() = TestCase {\n" % class_name
    res += "%sGiven(\"%s with custom params\") {\n" % (TAB * 2, class_name)
    all_fields = ""
    for field in fields:
        if not is_required(field):
            all_fields += "%s.%s(%s)\n" % (TAB * 4, field['name'], get_first_field_actual_value_except_default(field))
    res += "%sval %s = %s.Builder(%s)\n%s%s.build()\n" % (TAB * 3, class_name_lower, class_name,add_required_field(fields, ''), all_fields, TAB * 4)

    for field in fields:
        if not is_required(field):
            res += "\n%sWhen(\"Get custom %s\") {\n" % (TAB * 3, field['name'])
            res += "%sval actualValue = %s.%s\n" % (TAB * 4, class_name_lower, field['name'])
            res += "%sThen(\"It should be <%s>\") {\n" % (TAB * 4, get_first_field_expected_value(field).replace("\"","\\\""))
            res += "%sassertEquals(%s, actualValue)\n" % (TAB * 5, get_first_field_expected_value(field))
            res += "%s}\n" % (TAB * 4)
            res += "%s}\n" % (TAB * 3)

    res += "%s}\n%s}\n" % (TAB * 2, TAB)

    return res


def generate_fields_test(class_name, fields):
    class_name_lower = class_name[0].lower() + class_name[1:]
    res = ""

    for field in fields:
        res += TAB + "@TestFactory\n"
        res += TAB + "fun `Check %s field for %s builder`() = TestCase {\n" % (field['name'], class_name)
        res += "%smapOf(\n" % (TAB * 2)
        for test_value in field['testValues']:
            res += "%s%s to %s,\n" % (TAB * 3, test_value["actual"], test_value["expected"])
        res = res[:-2] + "\n"
        res += "%s).forEach { (inputValue, expectedValue) ->\n\n" % (TAB * 2)

        res += "%sGiven(\"%s with %s = $inputValue\") {\n" % (TAB * 3, class_name, field['name'])
        if not is_required(field):
            res += "%sval %s = %s.Builder(%s).%s(inputValue).build()\n\n" % (
            TAB * 4, class_name_lower, class_name, add_required_field(fields, field['name']), field['name'])
        else:
            res += "%sval %s = %s.Builder(%s).build()\n\n" % (
            TAB * 4, class_name_lower, class_name, add_required_field(fields, ''))

        res += "%sWhen(\"Get %s\") {\n" % (TAB * 4, field['name'])
        res += "%sval actualValue = %s.%s\n" % (TAB * 5, class_name_lower, field['name'])
        res += "%sThen(\"It should be <$expectedValue>\") {\n" % (TAB * 5)
        res += "%sassertEquals(expectedValue, actualValue)\n" % (TAB * 6)
        res += "%s}\n" % (TAB * 5)
        res += "%s}\n" % (TAB * 4)
        res += "%s}\n%s}\n%s}\n\n" % (TAB * 3, TAB * 2, TAB)

    return res


def generate_builder(klass):
    class_name = klass['className']
    class_package = klass['package']
    class_imports = klass['imports']
    class_fields = klass['fields']

    class_result = generate_class_package(class_package)
    class_result += generate_import(class_imports)
    class_result += generate_class_name(class_name)
    class_result += generate_class_fields(class_fields)
    class_result += generate_companion_object(class_fields)
    class_result += generate_class_constructor(class_fields)
    class_result += generate_builder_fields(class_fields)
    class_result += generate_builder_methods(class_fields)
    class_result += generate_builder_method(class_name, class_fields)

    return class_result


def generate_tests(klass):
    class_name = klass['className']
    class_package = klass['package']
    class_test_imports = klass['testImports']
    class_fields = klass['fields']

    test_result = generate_class_package(class_package)
    test_result += generate_test_import(class_test_imports)
    test_result += generate_test_name(class_name)
    test_result += generate_public_class_api_test(class_name, class_fields)
    test_result += generate_public_builder_api_test(class_name, class_fields)
    test_result += generate_default_test(class_name, class_fields)
    test_result += generate_fields_test(class_name, class_fields)
    test_result += generate_custom_test(class_name, class_fields)
    test_result += "}\n"

    return test_result


if __name__ == '__main__':
    config_file = read_config_file()
    for klass in config_file['classes']:
        print "\n" + "="*80+"\n"
        print klass['className']
        print "\n" + "="*80+"\n"

        file_path = config_file['scrPath'] + klass['package'].replace('.','/') + "/"+klass['className']+".kt"
        print file_path
        save_file(file_path, generate_builder(klass))

        test_file_path = config_file['testPath'] + klass['package'].replace('.','/') + "/"+klass['className']+"Test.kt"
        print test_file_path
        save_file(test_file_path, generate_tests(klass))

    print "\n" + "="*80+"\n"
    print "DONE\n\n"