/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.pgsql;

import io.liveoak.spi.ResourceNotFoundException;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.state.ResourceState;
import org.junit.Assert;
import org.junit.Test;

/**
 * See superclass JavaDoc for how to set up PostgreSQL for this test.
 *
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PgSqlCreateCollectionTest extends BasePgSqlTest {

    @Test
    public void testCreateCollection() throws Exception {
        if (skipTests()) {
            return;
        }
        String endpoint = "/testApp/" + BASEPATH;

        // list existing tables as unexpanded members
        ResourceState result = client.read(ctx("*"), endpoint);
        System.out.println(result);

        ResourceState expected = resource(BASEPATH, "/testApp", new Object[] {
                "links", list(
                        obj("rel", "batch",
                            "href", endpoint + "/_batch")
                ),
                "count", 3,
                "type", "database"},
                resource("addresses", endpoint, new Object[]{}),
                resource(schema + ".orders", endpoint, new Object[]{}),
                resource(schema_two + ".orders", endpoint, new Object[]{})
        );

        checkResource(result, expected);


        ResourceState body = resource("items", endpoint, new Object[]{
                "columns", list(
                    obj("name", "item_id",
                            "type", "varchar",
                            "size", 40),
                    obj("name", "name",
                            "type", "varchar",
                            "size", 255,
                            "nullable", false),
                    obj("name", "quantity",
                            "type", "int4",
                            "nullable", false),
                    obj("name", "price",
                            "type", "int4",
                            "nullable", false),
                    obj("name", "vat",
                            "type", "int4",
                            "nullable", false),
                    obj("name", "order_id",
                            "type", "varchar",
                            "size", 40,
                            "nullable", false)

                ),

                "primary-key", list("item_id"),

                "foreign-keys", list(
                    obj("table", schema_two + ".orders",
                            "columns", list("order_id"))
                )
        });

        result = client.create(ctx("*"), endpoint, body);
        System.out.println(result);

        // TODO: items;schema in response is invalid - it should be items/;schema
        ResourceState schemaBody = resource("items;schema", endpoint, new Object[] {
                "columns", list(
                        obj("name", "item_id",
                                "type", "varchar",
                                "size", 40,
                                "nullable", false,
                                "unique", true),
                        obj("name", "name",
                                "type", "varchar",
                                "size", 255,
                                "nullable", false,
                                "unique", false),
                        obj("name", "quantity",
                                "type", "int4",
                                "size", 10,
                                "nullable", false,
                                "unique", false),
                        obj("name", "price",
                                "type", "int4",
                                "size", 10,
                                "nullable", false,
                                "unique", false),
                        obj("name", "vat",
                                "type", "int4",
                                "size", 10,
                                "nullable", false,
                                "unique", false),
                        obj("name", "order_id",
                                "type", "varchar",
                                "size", 40,
                                "nullable", false,
                                "unique", false)

                ),

                "primary-key", list("item_id"),

                "foreign-keys", list(
                        obj("table", schema_two + ".orders",
                        "columns", list("order_id"))
                ),
                "ddl", "CREATE TABLE \"" + schema + "\".\"items\" (\"item_id\" varchar (40), \"name\" varchar (255) NOT NULL, \"quantity\" int4 NOT NULL, \"price\" int4 NOT NULL, \"vat\" int4 NOT NULL, \"order_id\" varchar (40) NOT NULL, PRIMARY KEY (\"item_id\"), FOREIGN KEY (\"order_id\") REFERENCES \"" + schema_two + "\".\"orders\" (\"order_id\"))"
                }
        );
        expected = schemaBody;
        checkResource(result, expected);


        // list existing tables as unexpanded members
        // there is now an extra table there
        // list existing tables as unexpanded members
        result = client.read(ctx("*"), endpoint);
        System.out.println(result);

        expected = resource(BASEPATH, "/testApp", new Object[] {
                        "links", list(
                                obj("rel", "batch",
                                    "href", endpoint + "/_batch")
                        ),
                        "count", 4,
                        "type", "database"},

                        resource("addresses", endpoint, new Object[]{}),
                        resource("items", endpoint, new Object[]{}),
                        resource(schema + ".orders", endpoint, new Object[]{}),
                        resource(schema_two + ".orders", endpoint, new Object[]{})
        );
        checkResource(result, expected);


        // read schema:
        String schemaEndpoint = endpoint + "/items;schema";
        ResourcePath path = new ResourcePath(schemaEndpoint);
        result = client.read(ctx("*", path), endpoint + "/items;schema");
        System.out.println(result);

        expected = schemaBody;
        checkResource(result, expected);


        // create a new item, linking to the first order
        endpoint = endpoint + "/items";
        body = resource("I39845355", endpoint, new Object[] {
                "name", "The Gadget",
                "quantity", 1,
                "price", 39900,
                "vat", 20,
                "order", resourceRef("/testApp/" + BASEPATH + "/" + schema_two + ".orders/014-2004096")
        });
        result = client.create(ctx("*(*)"), endpoint, body);
        System.out.println(result);

        expected = resource("I39845355", endpoint, new Object[] {
                "item_id", "I39845355",
                "name", "The Gadget",
                "quantity", 1,
                "price", 39900,
                "vat", 20,
                "order", resource("014-2004096", "/testApp/" + BASEPATH + "/" + schema_two + ".orders", new Object[]{
                        "order_id", "014-2004096",
                        "create_date", time("2014-04-02 11:06:12.0"),
                        "total", 43800L,
                        "items", list(resourceRef("/testApp/" + BASEPATH + "/items/I39845355")),
                        "address", resourceRef("/testApp/" + BASEPATH + "/addresses/2")
                })
        });

        checkResource(result, expected);


        // update the item to link to the second order, and have smaller price

        body = resource("I39845355", endpoint, new Object[] {
                "name", "The Gadget",
                "quantity", 1,
                "price", 32500,
                "vat", 20,
                "order", resourceRef("014-2004345", "/testApp/" + BASEPATH + "/" + schema_two + ".orders")
        });

        result = client.update(ctx("*(*)"), endpoint + "/I39845355", body);
        System.out.println(result);

        expected = resource("I39845355", endpoint, new Object[] {
                "item_id", "I39845355",
                "name", "The Gadget",
                "quantity", 1,
                "price", 32500,
                "vat", 20,
                "order", resource("014-2004345", "/testApp/" + BASEPATH + "/" + schema_two + ".orders", new Object[]{
                        "order_id", "014-2004345",
                        "create_date", time("2014-06-01 18:06:12.0"),
                        "total", 32500L,
                        "items", list(resourceRef("/testApp/" + BASEPATH + "/items/I39845355")),
                        "address", resourceRef("/testApp/" + BASEPATH + "/addresses/2")
                })
        });

        checkResource(result, expected);

        // remove the item
        result = client.delete(ctx("*(*)"), endpoint + "/I39845355");
        System.out.println(result);

        // try read it
        try {
            result = client.read(ctx("*(*)"), endpoint + "/I39845355");
            Assert.fail("Failed to delete item.");
        } catch (ResourceNotFoundException ignored) {}


        // remove items table
        result = client.delete(ctx("*"), endpoint);
        System.out.println(result);


        // list tables again
        endpoint = "/testApp/" + BASEPATH;

        // list existing tables as unexpanded members
        result = client.read(ctx("*"), endpoint);
        System.out.println(result);

        expected = resource(BASEPATH, "/testApp", new Object[] {
                "links", list(
                        obj("rel", "batch",
                            "href", endpoint + "/_batch")
                ),
                "count", 3,
                "type", "database"},

                resource("addresses", endpoint, new Object[]{}),
                resource(schema + ".orders", endpoint, new Object[]{}),
                resource(schema_two + ".orders", endpoint, new Object[]{})
        );

        checkResource(result, expected);
    }
}
