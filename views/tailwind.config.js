/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/main/jte/**/*.jte"],
  theme: {
    extend: {},
  },
  plugins: [require("@tailwindcss/typography"), require("daisyui")],
  daisyui: {
    themes: ["lemonade", "coffee"],
    darkTheme: "coffee"
  }
}

