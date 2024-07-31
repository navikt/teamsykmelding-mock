const naviktTailwindPreset = require("@navikt/ds-tailwind");

/** @type {import('tailwindcss').Config} */
export default {
  presets: [naviktTailwindPreset],
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {},
  },
  plugins: [],
};
