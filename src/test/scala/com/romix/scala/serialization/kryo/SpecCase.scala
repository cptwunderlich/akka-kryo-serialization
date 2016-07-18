package com.romix.scala.serialization.kryo

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.util.MapReferenceResolver
import org.objenesis.strategy.StdInstantiatorStrategy
import org.scalatest.FlatSpec

abstract class SpecCase extends FlatSpec {
  var kryo: Kryo = null
  
  val useSubclassResolver:Boolean = false

  override def withFixture(test: NoArgTest) = {
    val referenceResolver = new MapReferenceResolver()
    if (useSubclassResolver)
      kryo = new Kryo(new SubclassResolver(), referenceResolver)
    else
      kryo = new Kryo(referenceResolver)
    kryo.setReferences(true)
    kryo.setAutoReset(false)
    // Support deserialization of classes without no-arg constructors
    kryo.setInstantiatorStrategy(new StdInstantiatorStrategy())
    super.withFixture(test)
  }

  def roundTrip[T](length: Int, obj: T): T = {
    val outStream = new ByteArrayOutputStream()
    val output = new Output(outStream, 4096)
    kryo.writeClassAndObject(output, obj)
    output.flush()

    val input = new Input(new ByteArrayInputStream(outStream.toByteArray), 4096)
    val obj1 = kryo.readClassAndObject(input)

    assert(obj == obj1)

    obj1.asInstanceOf[T]
  }
}
