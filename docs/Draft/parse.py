import re

af = '* <a href=https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/{}>{}</a><br/>'

with open('./default_text.txt', 'r') as f:
    ret = []
    ref = []
    for line in f.readlines():
        code = line[:3]
        text = line[4:].strip()
        ret.append('"{}": "{}"'.format(code, text))
        ref.append(af.format(code, line.strip()))
    print(",\n".join(ret))

    print("\n".join(ref))