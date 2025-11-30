export function formatDate(input) {
  if (!input) return ''
  const s = Array.isArray(input) ? input[0] : input
  const dt = new Date(s)
  const y = dt.getFullYear()
  const m = String(dt.getMonth() + 1).padStart(2, '0')
  const d = String(dt.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}
