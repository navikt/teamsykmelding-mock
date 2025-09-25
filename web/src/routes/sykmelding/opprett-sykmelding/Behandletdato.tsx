import { ReactElement } from 'react'
import { useController } from 'react-hook-form'
import { DatePicker, useDatepicker } from '@navikt/ds-react'
import { format } from 'date-fns'

import { toDate } from '../../../utils/date.ts'

import { SykmeldingFormValues } from './OpprettSykmeldingForm.tsx'

function Behandletdato(): ReactElement {
    const { field } = useController<SykmeldingFormValues, 'behandletDato'>({
        name: 'behandletDato',
    })

    const { datepickerProps, inputProps } = useDatepicker({
        today: new Date(),
        defaultSelected: field.value ? toDate(field.value) : undefined,
        onDateChange: (date: Date | undefined) => {
            field.onChange(date ? format(date, 'yyyy-MM-dd') : undefined)
        },
    })

    return (
        <DatePicker {...datepickerProps}>
            <DatePicker.Input id={field.name} {...inputProps} label="Behandlingsdato" placeholder="DD.MM.ÅÅÅÅ" />
        </DatePicker>
    )
}

export default Behandletdato
