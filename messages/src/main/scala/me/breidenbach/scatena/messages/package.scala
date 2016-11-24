package me.breidenbach.scatena

/**
  * @author Kevin Breidenbach
  * Date: 10/3/16.
  */
package object messages {
  implicit class IntToBase( val digits:String ) extends AnyVal {
    def base(b:Int): Int = Integer.parseInt( digits, b )
    def b: Int = base(2)
    def o: Int = base(8)
    def x: Int = base(16)
  }
}
