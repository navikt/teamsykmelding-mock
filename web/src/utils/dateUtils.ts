import { formatISO, parseISO, sub } from 'date-fns'

export function toDate(date: string | Date): Date {
    return typeof date === 'string' ? parseISO(date) : date
}

export function subDays(date: string | Date, days: number): string {
    return formatISO(sub(toDate(date), { days }), { representation: 'date' })
}
