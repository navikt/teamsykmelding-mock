import { Alert, Button, Heading, TextField } from '@navikt/ds-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';

interface FormValues {
    fnr: string;
}

function OpprettUtenlandskPapirsykmelding(): JSX.Element {
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<FormValues>();
    const [error, setError] = useState<string | null>(null);
    const [result, setResult] = useState<string | null>(null);
    const OPPRETT_SYKMELDING_URL = `/api/proxy/papirsykmelding/utenlandsk/opprett`;

    const postData = async (data: FormValues): Promise<void> => {
        const response = await fetch(OPPRETT_SYKMELDING_URL, {
            method: 'POST',
            headers: {
                'Sykmeldt-Fnr': data.fnr,
            },
        });

        if (response.ok) {
            setResult((await response.json()).message);
        } else {
            setError((await response.json()).message);
        }
    };

    return (
        <form onSubmit={handleSubmit(postData)}>
            <Heading size="medium" level="2">
                Opprett utenlandsk papirsykmelding
            </Heading>
            <p />
            <TextField
                {...register('fnr', { required: true })}
                label="Fødselsnummer"
                error={errors.fnr && 'Fødselsnummer mangler'}
            />
            <p />
            <Button type="submit">Opprett</Button>
            <p />
            {error && <Alert variant="error">{error}</Alert>}
            {result && <Alert variant="success">{result}</Alert>}
        </form>
    );
}

export default OpprettUtenlandskPapirsykmelding;
