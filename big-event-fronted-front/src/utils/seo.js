/**
 * SEO工具函数
 * 用于动态更新页面的SEO元数据
 */

/**
 * 更新页面SEO信息
 * @param {Object} options SEO配置
 * @param {string} options.title 页面标题
 * @param {string} options.description 页面描述
 * @param {string} options.keywords 关键词
 * @param {string} options.image 分享图片
 * @param {string} options.url 页面URL
 */
export function updateSEO(options = {}) {
  const {
    title = 'BigEvent - 现代化博客系统',
    description = 'BigEvent - 一个现代化的博客系统，分享技术、生活、思考',
    keywords = '博客,技术,分享,BigEvent',
    image = '',
    url = window.location.href
  } = options

  // 更新基础meta标签
  updateMetaTag('title', title)
  updateMetaTag('description', description, 'name')
  updateMetaTag('keywords', keywords, 'name')

  // 更新Open Graph标签
  updateMetaTag('og:title', title, 'property')
  updateMetaTag('og:description', description, 'property')
  updateMetaTag('og:url', url, 'property')
  if (image) {
    updateMetaTag('og:image', image, 'property')
  }

  // 更新Twitter Card标签
  updateMetaTag('twitter:title', title, 'name')
  updateMetaTag('twitter:description', description, 'name')
  if (image) {
    updateMetaTag('twitter:image', image, 'name')
  }
}

/**
 * 更新或创建meta标签
 * @param {string} name 标签名称
 * @param {string} content 标签内容
 * @param {string} attr 属性类型（name或property）
 */
function updateMetaTag(name, content, attr = 'name') {
  if (!content) return

  let meta = document.querySelector(`meta[${attr}="${name}"]`)

  if (!meta) {
    meta = document.createElement('meta')
    meta.setAttribute(attr, name)
    document.head.appendChild(meta)
  }

  meta.setAttribute('content', content)
}

/**
 * 更新页面标题
 * @param {string} title 标题
 */
export function updateTitle(title) {
  document.title = title
  updateMetaTag('og:title', title, 'property')
  updateMetaTag('twitter:title', title, 'name')
}

/**
 * 重置SEO为默认值
 */
export function resetSEO() {
  updateSEO({
    title: 'BigEvent - 现代化博客系统',
    description: 'BigEvent - 一个现代化的博客系统，分享技术、生活、思考',
    keywords: '博客,技术,分享,BigEvent'
  })
}

