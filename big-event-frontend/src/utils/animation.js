// 动画工具函数
export const animateElement = (element, animation, duration = 300) => {
  return new Promise((resolve) => {
    element.classList.add(animation)
    
    setTimeout(() => {
      element.classList.remove(animation)
      resolve()
    }, duration)
  })
}

export const scrollToTop = (behavior = 'smooth') => {
  window.scrollTo({
    top: 0,
    behavior
  })
}

export const createParticles = (containerId, count = 50) => {
  const container = document.getElementById(containerId)
  if (!container) return
  
  for (let i = 0; i < count; i++) {
    const particle = document.createElement('div')
    particle.className = 'particle'
    particle.style.left = Math.random() * 100 + '%'
    particle.style.top = Math.random() * 100 + '%'
    particle.style.animationDelay = Math.random() * 2 + 's'
    particle.style.animationDuration = (Math.random() * 3 + 2) + 's'
    container.appendChild(particle)
  }
}

export const observeIntersection = (element, callback, options = {}) => {
  const defaultOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
  }
  
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        callback(entry.target)
        observer.unobserve(entry.target)
      }
    })
  }, { ...defaultOptions, ...options })
  
  observer.observe(element)
  
  return () => observer.disconnect()
}