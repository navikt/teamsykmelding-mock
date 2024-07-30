import { ReactElement, PropsWithChildren } from 'react'
import { useController } from 'react-hook-form'
import { BodyLong, Button, ErrorMessage, Label, Select } from '@navikt/ds-react'
import cn from 'clsx'
import { TrashIcon } from '@navikt/aksel-icons'

import DiagnoseTypeahead from './DiagnoseTypeahead/DiagnoseTypeahead'
import styles from './DiagnosePicker.module.css'

interface Props {
    name: 'hoveddiagnose' | `bidiagnoser.${number}`
    diagnoseType: 'hoveddiagnose' | 'bidiagnose'
    onRemove?: () => void
}

function DiagnosePicker({ name, diagnoseType, onRemove, children }: PropsWithChildren<Props>): ReactElement {
    const { field, fieldState } = useController({
        name,
        rules: {
            validate: (value) => {
                if (value.code == null) return `Du må velge en diagnosekode for ${diagnoseType}`
            },
        },
    })
    return (
        <div>
            <div className={styles.diagnosePicker}>
                <Select
                    label="Kodesystem"
                    onChange={(event) => {
                        field.onChange({ system: event.target.value, code: null, text: null })
                    }}
                >
                    <option>ICD10</option>
                    <option>ICPC2</option>
                </Select>
                <DiagnoseTypeahead
                    id={diagnoseType}
                    system={field.value.system.toLowerCase()}
                    onSelect={(suggestion) => field.onChange({ ...suggestion, system: field.value.system })}
                />
                <DiagnoseDescription text={field.value.text} />
                {onRemove && (
                    <div className={styles.onRemoveButtonWrapper}>
                        <Button variant="tertiary" icon={<TrashIcon />} type="button" onClick={onRemove} />
                    </div>
                )}
            </div>
            {children}
            {fieldState.error && <ErrorMessage>{fieldState.error.message}</ErrorMessage>}
        </div>
    )
}

function DiagnoseDescription({ text }: { text: string | null | undefined }): ReactElement {
    return (
        <div className={cn('navds-form-field navds-form-field--medium')}>
            <Label>Beskrivelse</Label>
            <BodyLong className={styles.diagnoseDescriptionText}>{text ?? '-'}</BodyLong>
        </div>
    )
}

export type Diagnose = {
    code: string
    system: DiagnoseSystem
    text: string
}

export type DiagnoseSystem = 'icd10' | 'icpc2'

export default DiagnosePicker
