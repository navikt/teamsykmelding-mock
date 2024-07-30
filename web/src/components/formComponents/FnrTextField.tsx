import React, { forwardRef, ReactElement } from 'react'
import { TextField, TextFieldProps } from '@navikt/ds-react'

const FNR_KEY = 'form-fnr'

const FnrTextField = forwardRef<HTMLInputElement, TextFieldProps>((props, ref): ReactElement => {
    const storedFnr = typeof window !== 'undefined' ? localStorage.getItem(FNR_KEY) ?? '' : ''

    return (
        <TextField
            ref={ref}
            {...props}
            defaultValue={storedFnr}
            onChange={(event) => {
                localStorage.setItem(FNR_KEY, event.target.value)
            }}
        ></TextField>
    )
})

FnrTextField.displayName = 'FnrTextField'

export default FnrTextField
