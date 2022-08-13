export interface Level {
  depth: number
  text: string
  children: Level[]
}

export interface SimpleRoster {
  styles: string[]
  root: Level
}

const parse = (text:string) :SimpleRoster => {
  return {
    styles: [],
    root: {depth:0,text:'',children:[]}
  };
}
