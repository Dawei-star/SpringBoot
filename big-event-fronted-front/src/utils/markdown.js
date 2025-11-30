import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js/lib/core'
import javascript from 'highlight.js/lib/languages/javascript'
import python from 'highlight.js/lib/languages/python'
import java from 'highlight.js/lib/languages/java'
import css from 'highlight.js/lib/languages/css'
import xml from 'highlight.js/lib/languages/xml'
import bash from 'highlight.js/lib/languages/bash'
import json from 'highlight.js/lib/languages/json'
import sql from 'highlight.js/lib/languages/sql'

// Register languages
hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('python', python)
hljs.registerLanguage('java', java)
hljs.registerLanguage('css', css)
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('html', xml)
hljs.registerLanguage('bash', bash)
hljs.registerLanguage('json', json)
hljs.registerLanguage('sql', sql)

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
          hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
          '</code></pre>'
      } catch { // ignore
      }
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  }
})

export const renderMarkdown = (content) => {
  if (!content) return ''
  return md.render(content)
}

export const extractTOC = (content) => {
  if (!content) return []
  const headers = []
  const lines = content.split('\n')
  const reg = /^(#{1,6})\s+(.+)$/

  lines.forEach((line, index) => {
    const match = line.match(reg)
    if (match) {
      const level = match[1].length
      const text = match[2].trim()
      const id = `heading-${index}`
      headers.push({ level, text, id })
    }
  })
  return headers
}

// Custom renderer to add IDs to headers
md.renderer.rules.heading_open = (tokens, idx) => {
  const token = tokens[idx]
  return `<${token.tag} id="heading-${idx}" class="article-heading">`
}

