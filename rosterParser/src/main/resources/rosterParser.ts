declare interface Level {
    getChildren(): Level[]
    getText(): string
    getHeader(): number?
    isAnnotation(): boolean
    getCost(): number
    getMultiplier(): number
    getTotal(): number
}

declare interface ParsedRoster {
    getStyles(): string[]
    getRoot(): Level
}
declare function parseRoster(text:string): ParsedRoster;