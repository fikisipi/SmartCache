import markdown

def build_md(filename):
    with open(filename, 'r') as md_file:
        raw_md = md_file.read()
    
    md = markdown.Markdown(extensions=['markdown.extensions.toc'])
    html = md.convert(raw_md).replace('<code>', '<code class="java">')
    
    return {'html': html, 'toc': md.toc}

def main():
    with open('template.html', 'r') as template_file:
        template_html = template_file.read()
    
    markdown_html = build_md('index.md')
    template_html = template_html.replace('%index%', markdown_html['html']).replace('%toc%', markdown_html['toc'])
    
    with open('index.html', 'w') as index_file:
        index_file.write(template_html)
if __name__ == '__main__':
    main()