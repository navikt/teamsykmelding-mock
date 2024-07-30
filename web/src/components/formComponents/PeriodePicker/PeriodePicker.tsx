import { ReactElement } from 'react'
import { useController } from 'react-hook-form'
import { DatePicker, useRangeDatepicker } from '@navikt/ds-react'
import { format } from 'date-fns'

import { toDate } from '../../../utils/dateUtils'

import styles from './PeriodePicker.module.css'

type DateRange = {
    from: Date | undefined
    to?: Date | undefined
}

type FormName = `perioder.${number}`

interface PeriodePickerProps {
    name: FormName
}

function PeriodePicker({ name }: PeriodePickerProps): ReactElement {
    const { field: fromField } = useController({
        name: `${name}.fom`,
        rules: { required: 'Du må fylle inn fra-dato.' },
    })
    const { field: toField } = useController({
        name: `${name}.tom`,
        rules: { required: 'Du må fylle inn til-dato.' },
    })

    const { datepickerProps, toInputProps, fromInputProps } = useRangeDatepicker({
        defaultSelected: {
            from: fromField.value ? toDate(fromField.value) : undefined,
            to: toField.value ? toDate(toField.value) : undefined,
        },
        onRangeChange: (value: DateRange | undefined) => {
            fromField.onChange(value?.from ? format(value.from, 'yyyy-MM-dd') : undefined)
            toField.onChange(value?.to ? format(value.to, 'yyyy-MM-dd') : undefined)
        },
    })

    return (
        <div className={styles.periodePicker}>
            <DatePicker {...datepickerProps} wrapperClassName={styles.dateRangePicker}>
                <DatePicker.Input id={fromField.name} {...fromInputProps} label="Fra" placeholder="DD.MM.ÅÅÅÅ" />
                <DatePicker.Input id={toField.name} {...toInputProps} label="Til" placeholder="DD.MM.ÅÅÅÅ" />
            </DatePicker>
        </div>
    )
}

export default PeriodePicker
