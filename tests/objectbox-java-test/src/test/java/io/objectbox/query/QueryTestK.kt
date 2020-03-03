package io.objectbox.query

import io.objectbox.TestEntity_
import io.objectbox.kotlin.and
import io.objectbox.kotlin.inValues
import io.objectbox.kotlin.or
import io.objectbox.kotlin.query
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests Kotlin extension functions syntax for queries works as expected.
 */
class QueryTestK : AbstractQueryTest() {

    @Test
    fun newQueryApi() {
        putTestEntity("Fry", 14)
        putTestEntity("Fry", 12)
        putTestEntity("Fry", 10)

        // current query API
        val query = box.query {
            less(TestEntity_.simpleInt, 12)
            or()
            inValues(TestEntity_.simpleLong, longArrayOf(1012))
            equal(TestEntity_.simpleString, "Fry")
            order(TestEntity_.simpleInt)
        }
        val results = query.find()
        assertEquals(2, results.size)
        assertEquals(10, results[0].simpleInt)
        assertEquals(12, results[1].simpleInt)

        // suggested query API
        val newQuery = box.query(
                (TestEntity_.simpleInt.less(12) or TestEntity_.simpleLong.oneOf(longArrayOf(1012)))
                        and TestEntity_.simpleString.equal("Fry")
        ).order(TestEntity_.simpleInt).build()
        val resultsNew = newQuery.find()
        assertEquals(2, resultsNew.size)
        assertEquals(10, resultsNew[0].simpleInt)
        assertEquals(12, resultsNew[1].simpleInt)

        val newQueryOr = box.query(
                // (EQ OR EQ) AND LESS
                (TestEntity_.simpleString.equal("Fry") or TestEntity_.simpleString.equal("Sarah"))
                        and TestEntity_.simpleInt.less(12)
        ).build().find()
        assertEquals(1, newQueryOr.size) // only the Fry age 10

        val newQueryAnd = box.query(
                // EQ OR (EQ AND LESS)
                TestEntity_.simpleString.equal("Fry") or
                        (TestEntity_.simpleString.equal("Sarah") and TestEntity_.simpleInt.less(12))
        ).build().find()
        assertEquals(3, newQueryAnd.size) // all Fry's
    }

    @Test
    fun intLessAndGreater() {
        putTestEntitiesScalars()
        val query = box.query(
                TestEntity_.simpleInt.greater(2003)
                        and TestEntity_.simpleShort.less(2107)
        ).build()
        assertEquals(3, query.count())
    }

    @Test
    fun intBetween() {
        putTestEntitiesScalars()
        val query = box.query(
                TestEntity_.simpleInt.between(2003, 2006)
        ).build()
        assertEquals(4, query.count())
    }

    @Test
    fun intOneOf() {
        putTestEntitiesScalars()

        val valuesInt = intArrayOf(1, 1, 2, 3, 2003, 2007, 2002, -1)
        val query = box.query(
                TestEntity_.simpleInt.oneOf(valuesInt).alias("int")
        ).build()
        assertEquals(3, query.count())

        val valuesInt2 = intArrayOf(2003)
        query.setParameters(TestEntity_.simpleInt, valuesInt2)
        assertEquals(1, query.count())

        val valuesInt3 = intArrayOf(2003, 2007)
        query.setParameters("int", valuesInt3)
        assertEquals(2, query.count())
    }


    @Test
    fun or() {
        putTestEntitiesScalars()
        val query = box.query(
                TestEntity_.simpleInt.equal(2007) or TestEntity_.simpleLong.equal(3002)
        ).build()
        val entities = query.find()
        assertEquals(2, entities.size.toLong())
        assertEquals(3002, entities[0].simpleLong)
        assertEquals(2007, entities[1].simpleInt.toLong())
    }

    @Test
    fun and() {
        putTestEntitiesScalars()
        // Result if OR precedence (wrong): {}, AND precedence (expected): {2008}
        val query = box.query(
                TestEntity_.simpleInt.equal(2006) and TestEntity_.simpleInt.equal(2007) or TestEntity_.simpleInt.equal(2008)
        ).build()
        val entities = query.find()
        assertEquals(1, entities.size.toLong())
        assertEquals(2008, entities[0].simpleInt.toLong())
    }

}