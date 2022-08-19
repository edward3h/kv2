export interface Level {
  text: string
  children: Level[]
  header?: number
  annotation?: boolean
  cost?: number
  multiplier?: number
  total: () => number
}

export interface SimpleRoster {
  styles: string[]
  root: Level
}

const BLANK = /^\s*$/;
const INDENTS = /^\s*/;
const POINTS = /\s\[\s*(\d+)\s*\]/;
const MULTIPLIER = /\s[x*]\s*(\d+)/;
const ANNOTATION = /.+\:.+/;
const HEADER = /^\s*(\#+)\s*(.+)/;
const STYLE = /^\s*\!\s*(.+)/;

type NestedLineInfo = {kind:'header',header:number,text:string,indent:number} | {kind:'line',text:string,indent:number,annotation:boolean,cost:number,multiplier:number}
type LineInfo = {kind:'empty'} | {kind:'style',text:string} | NestedLineInfo

const parseLineInfo = (line: string): LineInfo => {
  let m
  if (line.match(BLANK)) {
    return {kind:'empty'}
  }
  if (m = line.match(STYLE)) {
    return {kind:'style', text:m[1]}
  }
  let indent = 0
  if (m = line.match(INDENTS)) {
    indent = m[0].length
  }
  if (m = line.match(HEADER)) {
    return {kind: 'header', header:m[1].length, text:m[2], indent}
  }
  const r = {
    text: line.trim(),
    indent,
    annotation: Boolean(line.match(ANNOTATION)),
    cost: 0,
    multiplier: 1
  }
  if (m = line.match(POINTS)) {
    r.cost = parseInt(m[1], 10)
    if (m = line.match(MULTIPLIER)) {
      r.multiplier = parseInt(m[1], 10)
    }
  }
  return { kind: 'line', ...r }
}

const getIndents = (lineInfos: LineInfo[]) => {
  const s = new Set<number>()
  lineInfos.forEach(li => {
    if (li.kind === 'line') {
      s.add(li.indent)
    }
  })
  if (s.size == 0) {
    s.add(0)
  }
  return Array.from(s)
}

function total(this:Level) {
  let sum = 0
  if (this.cost && this.multiplier) {
    sum += this.cost * this.multiplier
  }
  if (this.children) {
    sum += this.children.reduce((acc, v) => acc + v.total(), 0)
  }
  return sum
}

const levelFromLineInfo = (li:NestedLineInfo): Level => {
  if (li.kind === 'header') {
    return {header: li.header, text: li.text, children: [], total}
  }
  return {
    text: li.text,
    annotation: li.annotation,
    cost: li.cost,
    multiplier: li.multiplier,
    children: [],
    total
  }
}

const build = (lineInfos: NestedLineInfo[]): Level => {
  const indents = getIndents(lineInfos)
  const depthMap = new Map()
  indents.forEach((value, index) => depthMap.set(value, index))
  const root = {text:'', children:[], total}
  let depth = 0
  const parents: Level[] = [root]
  let last:Level = root
  lineInfos.forEach(li => {
    const liDepth = depthMap.get(li.indent)
    const liLevel = levelFromLineInfo(li)
    if (liDepth < depth) {
      parents.pop()
    }
    if (liDepth > depth) {
      parents.push(last)
    }
    parents.at(-1)?.children.push(liLevel)
    depth = liDepth
    last = liLevel
  })

  return root
}

const parse = (text:string):SimpleRoster => {
  const lines = text.split('\n')
  const lineInfos = lines.map(parseLineInfo).filter(li => li.kind !== 'empty')
  const styles = lineInfos.flatMap(li => li.kind === 'style' ? [li.text] : [])
  const root = build(lineInfos.filter(li => li.kind === 'line' || li.kind === 'header') as NestedLineInfo[])
  return {
    styles,
    root
  };
}

export default parse;
