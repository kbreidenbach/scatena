package me.breidenbach.scatena

/**
  * @author kbreidenbach 
  *         Date: 10/3/16.
  */
package object util {
  implicit class IntToBase( val digits:String ) extends AnyVal {
    def base(b:Int) = Integer.parseInt( digits, b )
    def b = base(2)
    def o = base(8)
    def x = base(16)
  }
}
