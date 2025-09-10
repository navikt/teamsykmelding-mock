import { ReactElement } from "react";
import { FormProvider, useForm } from "react-hook-form";
import { useMutation } from "@tanstack/react-query";
import { post, SimpleMessage } from "../../../api/fetcherInputDolly.ts";
import { SykmeldingDollyValues } from "./Sykmelding.ts";
import FnrTextField from "../../../components/form/FnrTextField.tsx";
import styles from "../../sykmelding/opprett-sykmelding/OpprettSykmelding.module.css";
import FormPage from "../../../components/layout/FormPage.tsx";
import { Button } from "@navikt/ds-react";
import AktivitetPicker from "./AktivitetPicker.tsx";

function OpprettSykmeldingDolly(): ReactElement {
    const form = useForm<SykmeldingDollyValues>({
        defaultValues: {
            ident: '',
            aktivitet: {
                fom: undefined,
                tom: undefined
            }
        },
    })

    const opprettSykmeldingMutation = useMutation({
        mutationFn: post<SykmeldingDollyValues, SimpleMessage>('/api/sykmelding'),
    })

    return (
        <FormProvider {...form}>
            <form
                onSubmit={form.handleSubmit((values) => {
                    opprettSykmeldingMutation.mutate(values)
                })}
            >
                <FormPage
                    title="Opprett ny nasjonal sykmelding"
                    mutations={[opprettSykmeldingMutation]}
                >
                    <FnrTextField
                        className={styles.commonFormElement}
                        {...form.register('ident', { required: true })}
                        label="Fødselsnummer"
                        error={form.formState.errors.ident && 'Fødselsnummer for pasienten mangler'}
                    />
                    <AktivitetPicker />
                    <FormPage.FormActions>
                        <Button
                            type="submit"
                            loading={opprettSykmeldingMutation.isPending}
                            disabled={opprettSykmeldingMutation.isPending}
                        >
                            Opprett
                        </Button>
                    </FormPage.FormActions>
                </FormPage>
            </form>
        </FormProvider>
    )
}

export default OpprettSykmeldingDolly
