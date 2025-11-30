import { describe, it, expect } from 'vitest'
import { formatDate } from '../../src/utils/date'

describe('formatDate', () => {
  it('formats ISO string', () => {
    expect(formatDate('2025-11-27T00:00:00Z')).toBe('2025-11-27')
  })

  it('formats array input', () => {
    expect(formatDate(['2025-01-02'])).toBe('2025-01-02')
  })

  it('handles falsy', () => {
    expect(formatDate(undefined)).toBe('')
  })
})
