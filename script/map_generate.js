for (var i = 1; i < 10; i++) {
    process.stdout.write('    public static Map<String, Object> map(');
    for (var x = 1; x <= i; x++) {
        if (x > 1) {
        process.stdout.write(',');
        }
        process.stdout.write(`String k${x}, Object v${x}`);
    }
    process.stdout.write(') {        return map0(');
       for (var x = 1; x <= i; x++) {
            if (x > 1) {
            process.stdout.write(',');
            }
            process.stdout.write(`k${x}, v${x}`);
        }
        process.stdout.write(');}')
}