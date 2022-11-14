import parse from '../../src/components/parser'
import { describe, test, expect } from '@jest/globals'

describe('testing parser', () => {
  test('empty text gives empty root', () => {
    expect(parse('')).toMatchObject({ root: { text: '', children: [] }, styles: [] })
  })

  test('a style rule', () => {
    expect(parse('! .foo { width: 49%; }')).toMatchObject({ root: { text: '', children: [] }, styles: ['.foo { width: 49%; }'] })
  })

  test('a header rule', () => {
    expect(parse('# Header')).toMatchObject({ root: { text: '', children: [{ text: 'Header', header: 1, children: [] }] }, styles: [] })
  })

  test('nesting 1', () => {
    expect(parse(`
Detachment
 Category
    `)).toMatchObject({
      root: {
        children: [{
          text: 'Detachment',
          children: [{
            text: 'Category',
            children: []
          }]
        }]
      }
    })
  })

  test('nesting 2', () => {
    expect(parse(`
Detachment
  Category
    `)).toMatchObject({
      root: {
        children: [{
          text: 'Detachment',
          children: [{
            text: 'Category',
            children: []
          }]
        }]
      }
    })
  })

  test('nesting 3', () => {
    const roster = parse(`
Gang

Vehicles
 Road Thug [25]
  Mauler [100]
   Twin-linked Bolters [65]

 Road Thug [25]
  Heavy Vehicle [175]
   Wheeled
   Transport Bed [15]
   Nitro Burners [15]
   Heavy Stubber (Crew, Front/Right) [130]
    `)
    expect(roster.root.children).toHaveLength(2)
    const vehicles = roster.root.children[1]
    expect(vehicles.children).toHaveLength(2)
  })

  test('math 1', () => {
    const roster = parse(`
Detachment
  Category
    Unit
      Option
    `)
    expect(roster.root.total()).toEqual(0)
  })


  test('math 2', () => {
    const roster = parse(`
Detachment
  Category
    Unit x5 [10]
      Option [6]
    `)
    expect(roster.root.total()).toEqual(56)
  })


  test('math 3', () => {
    const roster = parse(`
Detachment
  Category
    Unit x5 [10]
      Option [6]
      Option [3]
    Unit 2 * 4 [12]
      Option [1] * 2

  Category
    Unit x3 [22]
    `)
    expect(roster.root.total()).toEqual(175)
  })
})
