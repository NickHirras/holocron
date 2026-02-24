/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        "./src/**/*.{html,ts}",
    ],
    theme: {
        extend: {
            colors: {
                holocron: {
                    base: '#0f172a', /* slate-900 */
                    surface: '#1e293b', /* slate-800 */
                    'surface-hover': '#334155', /* slate-700 */
                    'neon-blue': '#38bdf8', /* sky-400 */
                    'neon-blue-hover': '#7dd3fc', /* sky-300 */
                    'text-primary': '#f8fafc', /* slate-50 */
                    'text-secondary': '#94a3b8', /* slate-400 */
                }
            }
        },
    },
    plugins: [],
}
