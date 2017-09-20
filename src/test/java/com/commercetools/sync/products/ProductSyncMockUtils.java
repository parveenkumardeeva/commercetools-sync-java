package com.commercetools.sync.products;

import io.sphere.sdk.categories.Category;
import io.sphere.sdk.models.Reference;
import io.sphere.sdk.products.CategoryOrderHints;
import io.sphere.sdk.products.Product;
import io.sphere.sdk.products.ProductData;
import io.sphere.sdk.products.ProductDraft;
import io.sphere.sdk.products.ProductDraftBuilder;
import io.sphere.sdk.products.ProductVariantDraft;
import io.sphere.sdk.products.ProductVariantDraftBuilder;
import io.sphere.sdk.producttypes.ProductType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static io.sphere.sdk.json.SphereJsonUtils.readObjectFromResource;
import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toList;

public class ProductSyncMockUtils {
    public static final String PRODUCT_KEY_1_PUBLISHED_RESOURCE_PATH = "product-key-1-published.json";
    public static final String PRODUCT_KEY_1_UNPUBLISHED_RESOURCE_PATH = "product-key-1-unpublished.json";
    public static final String CATEGORY_KEY_1_RESOURCE_PATH = "category-key-1.json";

    /**
     * Builds a {@link ProductDraftBuilder} based on the current projection of the product JSON resource located at the
     * {@code jsonResourcePath} and based on the supplied {@code productType}.
     *
     * @param jsonResourcePath     the path of the JSON resource to build the product draft from.
     * @param productTypeReference the reference of the product type that the product draft belongs to.
     * @return a {@link ProductDraftBuilder} instance containing the data from the current projection of the specified
     *          JSON resource and the product type.
     */
    public static ProductDraftBuilder createProductDraftBuilder(@Nonnull final String jsonResourcePath,
                                                                @Nonnull final Reference<ProductType>
                                                                    productTypeReference) {
        final Product productFromJson = readObjectFromResource(jsonResourcePath, Product.class);
        final ProductData productData = productFromJson.getMasterData().getCurrent();

        @SuppressWarnings("ConstantConditions") final List<ProductVariantDraft> allVariants = productData
            .getAllVariants().stream()
            .map(productVariant -> ProductVariantDraftBuilder.of(productVariant).build())
            .collect(toList());

        return ProductDraftBuilder
            .of(productTypeReference, productData.getName(), productData.getSlug(), allVariants)
            .metaDescription(productData.getMetaDescription())
            .metaKeywords(productData.getMetaKeywords())
            .metaTitle(productData.getMetaTitle())
            .description(productData.getDescription())
            .searchKeywords(productData.getSearchKeywords())
            .key(productFromJson.getKey())
            .publish(productFromJson.getMasterData().isPublished())
            .categories(productData.getCategories())
            .categoryOrderHints(productData.getCategoryOrderHints());
    }


    /**
     * Given a {@link List} of {@link Category}, this method returns an instance of {@link CategoryOrderHints}
     * containing a {@link Map}, in which each entry has category id from the supplied {@link List} as a key and a
     * random categoryOrderHint which is a {@link String} containing a random double value between 0 and 1 (exclusive).
     *
     * <p>Note: The random double value is generated by the {@link ThreadLocalRandom#current()} nextDouble method.
     *
     * @param categoriesReferences list of references of categories to build categoryOrderHints for.
     * @return an instance of {@link CategoryOrderHints} containing a categoryOrderHint for each category in the
     * supplied list of categories.
     */
    public static CategoryOrderHints createRandomCategoryOrderHints(@Nonnull final List<Reference<Category>>
                                                                        categoriesReferences) {
        final Map<String, String> categoryOrderHints = new HashMap<>();
        categoriesReferences.forEach(categoryReference -> {
            final double randomDouble = ThreadLocalRandom.current().nextDouble(0, 1);
            categoryOrderHints.put(categoryReference.getId(), valueOf(randomDouble));
        });
        return CategoryOrderHints.of(categoryOrderHints);
    }

    /**
     * Builds a {@link ProductDraft} based on the current projection of the product JSON resource located at the
     * {@code jsonResourcePath} and based on the supplied {@code productType}. The method also attaches the created
     * {@link ProductDraft} to all the {@code categories} specified and assigns {@code categoryOrderHints} for it for
     * each category assigned.
     *
     * @param jsonResourcePath     the path of the JSON resource to build the product draft from.
     * @param productTypeReference the reference of the  product type that the product draft belongs to.
     * @param categoryReferences   the references to the categories to attach this product draft to.
     * @param categoryOrderHints   the categoryOrderHint for each category this product belongs to.
     * @return a {@link ProductDraft} instance containing the data from the current projection of the specified
     *         JSON resource and the product type. The draft would be assigned also to the specified {@code categories}
     *         with the supplied {@code categoryOrderHints}.
     */
    public static ProductDraft createProductDraft(@Nonnull final String jsonResourcePath,
                                                  @Nonnull final Reference<ProductType> productTypeReference,
                                                  @Nonnull final List<Reference<Category>> categoryReferences,
                                                  @Nullable final CategoryOrderHints categoryOrderHints) {
        return createProductDraftBuilder(jsonResourcePath, productTypeReference)
            .categories(categoryReferences)
            .categoryOrderHints(categoryOrderHints)
            .build();
    }
}
