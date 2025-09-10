import { ReactElement } from 'react'
import { useController } from 'react-hook-form'
import { DatePicker, useRangeDatepicker } from '@navikt/ds-react'
import { format } from 'date-fns'

import { toDate } from '../../../utils/date.ts'

type DateRange = {
    from: Date | undefined
    to?: Date | undefined
}

interface AktivitetPickerProps {
    name: `aktivitet.${number}`
}

function AktivitetPicker({ name }: AktivitetPickerProps): ReactElement {
    const { field: fromField, fieldState: fromState } = useController({
        name: `${name}.fom`,
        rules: { required: 'Du må fylle inn fra-dato.' },
    })
    const { field: toField, fieldState: toState } = useController({
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
        <div className="flex flex-wrap pt-4">
            <DatePicker {...datepickerProps} wrapperClassName="flex gap-4">
                <DatePicker.Input
                    {...fromInputProps}
                    id={fromField.name}
                    label="Fra"
                    placeholder="DD.MM.ÅÅÅÅ"
                    error={fromState.error?.message}
                />
                <DatePicker.Input
                    {...toInputProps}
                    id={toField.name}
                    label="Til"
                    placeholder="DD.MM.ÅÅÅÅ"
                    error={toState.error?.message}
                />
            </DatePicker>
        </div>
    )
}

export default AktivitetPicker
