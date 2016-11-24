package me.breidenbach.scatena.machina

import me.breidenbach.scatena.apparitio.Apparitio
import me.breidenbach.scatena.objecto.Objecto

import scala.collection.mutable

/**
  * @author Kevin Breidenbach 
  * Date: 11/17/16
  */
object VirtualApparatus {
  val objects = new mutable.HashSet[Objecto]()
  val applications = new mutable.HashSet[Apparitio]()



}
