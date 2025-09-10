import { ReactElement } from "react"
import { FormProvider, useFieldArray, useForm } from "react-hook-form"
import { DollySykmeldingResponse, SykmeldingDollyValues } from "./Sykmelding.ts"
import FnrTextField from "../../../components/form/FnrTextField.tsx"
import { Button } from "@navikt/ds-react"
import AktivitetPicker from "./AktivitetPicker.tsx"
import styles from "./OpprettSykmeldingDolly.module.css"
import { useAction } from "../../../proxy/api-hooks.ts"
import BasicPage from "../../../components/layout/BasicPage.tsx"
import ActionFeedback from "../../../proxy/action-feedback.tsx"
import OpprettetSykmelding from "./OpprettetSykmelding.tsx"

function OpprettSykmeldingDolly(): ReactElement {
    const form = useForm<SykmeldingDollyValues>({
        defaultValues: {
            ident: '',
            aktivitet: [{
                fom: '',
                tom: ''
            }]
        },
    })

    const control = form.control
    const {
        fields: aktivitetFields,
        append,
        remove,
    } = useFieldArray({
        control,
        name: 'aktivitet',
    })

    const [postData, { error, result, loading }] = useAction<SykmeldingDollyValues>('/sykmelding')
    const actionResponse = (result as unknown as DollySykmeldingResponse | null) ?? null

    return (
        <BasicPage title="Opprett sykmelding med input-dolly">
            <FormProvider {...form}>
                <form
                    onSubmit={form.handleSubmit((values) => {
                        postData(values)
                    })}
                >
                    <FnrTextField
                        className={styles.commonFormElement}
                        {...form.register('ident', { required: true })}
                        label="Fødselsnummer"
                        error={form.formState.errors.ident && 'Fødselsnummer for pasienten mangler'}
                    />
                    {aktivitetFields.map((aktivitet, index) => (
                        <div key={aktivitet.id}>
                            <AktivitetPicker name={`aktivitet.${index}`} />
                            <Button type="button" onClick={() => remove(index)} variant="tertiary">
                                Slett
                            </Button>
                        </div>
                        ))
                    }
                    <div className={styles.aktivitetButton}>
                        <Button
                            type="button"
                            onClick={() =>
                                append({
                                    fom: '',
                                    tom: '',
                                })
                            }
                        >
                            Legg til periode
                        </Button>

                    </div>
                    <ActionFeedback error={error} result={null}>
                        <Button type="submit" loading={loading}>
                            Opprett
                        </Button>
                    </ActionFeedback>
                </form>
            </FormProvider>
            {actionResponse !== null && <OpprettetSykmelding sykmelding={actionResponse}/>}
        </BasicPage>
    )
}

export default OpprettSykmeldingDolly
