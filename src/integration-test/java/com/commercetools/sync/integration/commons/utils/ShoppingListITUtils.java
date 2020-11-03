package com.commercetools.sync.integration.commons.utils;

import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.models.LocalizedString;

import io.sphere.sdk.shoppinglists.LineItemDraft;
import io.sphere.sdk.shoppinglists.ShoppingList;
import io.sphere.sdk.shoppinglists.ShoppingListDraft;
import io.sphere.sdk.shoppinglists.ShoppingListDraftBuilder;
import io.sphere.sdk.shoppinglists.TextLineItemDraft;
import io.sphere.sdk.shoppinglists.commands.ShoppingListCreateCommand;
import io.sphere.sdk.shoppinglists.commands.ShoppingListDeleteCommand;
import io.sphere.sdk.shoppinglists.queries.ShoppingListQuery;
import javax.annotation.Nonnull;
import java.util.List;

import static com.commercetools.sync.integration.commons.utils.ITUtils.queryAndExecute;
import static com.commercetools.sync.integration.commons.utils.CustomerITUtils.deleteCustomers;
import static com.commercetools.sync.integration.commons.utils.ProductITUtils.deleteAllProducts;
import static com.commercetools.sync.integration.commons.utils.ProductTypeITUtils.deleteProductTypes;

import static com.commercetools.tests.utils.CompletionStageUtil.executeBlocking;


public final class ShoppingListITUtils {

    /**
     * Deletes all shopping lists, products and product types from the CTP project defined by the {@code ctpClient}.
     *
     * @param ctpClient defines the CTP project to delete test data from.
     */
    public static void deleteShoppingListTestData(@Nonnull final SphereClient ctpClient) {
        deleteShoppingLists(ctpClient);
        deleteCustomers(ctpClient);
        deleteAllProducts(ctpClient);
        deleteProductTypes(ctpClient);
    }

    /**
     * Deletes all ShoppingLists from the CTP project defined by the {@code ctpClient}.
     *
     * @param ctpClient defines the CTP project to delete the ShoppingLists from.
     */
    public static void deleteShoppingLists(@Nonnull final SphereClient ctpClient) {
        queryAndExecute(ctpClient, ShoppingListQuery.of(), ShoppingListDeleteCommand::of);
    }


    /**
     * Creates a {@link ShoppingList} in the CTP project defined by the {@code ctpClient} in a blocking fashion.
     *
     * @param ctpClient defines the CTP project to create the ShoppingList in.
     * @param name      the name of the ShoppingList to create.
     * @param key       the key of the ShoppingList to create.
     * @return the created ShoppingList.
     */
    public static ShoppingList createShoppingList(@Nonnull final SphereClient ctpClient, @Nonnull final String name,
                                                    @Nonnull final String key) {
        final ShoppingListDraft shoppingListDraft = ShoppingListDraftBuilder.of(LocalizedString.ofEnglish(name))
                                                                            .key(key)
                                                                            .build();

        return executeBlocking(ctpClient.execute(ShoppingListCreateCommand.of(shoppingListDraft)));
    }

    /**
     * Creates a {@link ShoppingList} in the CTP project defined by the {@code ctpClient} in a blocking fashion.
     *
     * @param ctpClient defines the CTP project to create the ShoppingList in.
     * @param name      the name of the ShoppingList to create.
     * @param key       the key of the ShoppingList to create.
     * @param textLineItems     the list of TextLineItemDraft which ShoppingList contains.
     * @return the created ShoppingList.
     */
    public static ShoppingList createShoppingListWithTextLineItems(
            @Nonnull final SphereClient ctpClient,
            @Nonnull final String name,
            @Nonnull final String key,
            @Nonnull final List<TextLineItemDraft> textLineItems) {

        final ShoppingListDraft shoppingListDraft = ShoppingListDraftBuilder.of(LocalizedString.ofEnglish(name))
                .key(key)
                .textLineItems(textLineItems)
                .build();

        return executeBlocking(ctpClient.execute(ShoppingListCreateCommand.of(shoppingListDraft)));
    }

    /**
     * Creates a {@link ShoppingList} in the CTP project defined by the {@code ctpClient} in a blocking fashion.
     *
     * @param ctpClient defines the CTP project to create the ShoppingList in.
     * @param name      the name of the ShoppingList to create.
     * @param key       the key of the ShoppingList to create.
     * @param lineItems the list of LineItemDraft which ShoppingList contains.
     * @return the created ShoppingList.
     */
    public static ShoppingList createShoppingListWithLineItems(
            @Nonnull final SphereClient ctpClient,
            @Nonnull final String name,
            @Nonnull final String key,
            @Nonnull final List<LineItemDraft> lineItems) {

        final ShoppingListDraft shoppingListDraft = ShoppingListDraftBuilder.of(LocalizedString.ofEnglish(name))
                .key(key)
                .lineItems(lineItems)
                .build();

        return executeBlocking(ctpClient.execute(ShoppingListCreateCommand.of(shoppingListDraft)));
    }

    private ShoppingListITUtils() {
    }
}
