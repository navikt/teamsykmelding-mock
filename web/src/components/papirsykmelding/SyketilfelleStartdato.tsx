import { ReactElement } from 'react'
import { useController } from 'react-hook-form'
import { DatePicker, useDatepicker } from '@navikt/ds-react'
import { format } from 'date-fns'

import { toDate } from '../../utils/dateUtils'

import { PapirsykmeldingFormValues } from './OpprettPapirsykmelding'

function SyketilfelleStartdato(): ReactElement {
    const { field } = useController<PapirsykmeldingFormValues, 'syketilfelleStartdato'>({
        name: 'syketilfelleStartdato',
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
            <DatePicker.Input
                id={field.name}
                {...inputProps}
                label="Startdato på syketilfelle"
                placeholder="DD.MM.ÅÅÅÅ"
            />
        </DatePicker>
    )
}

export default SyketilfelleStartdato
