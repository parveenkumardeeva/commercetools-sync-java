package com.commercetools.sync.services.impl;


import com.commercetools.sync.categories.CategorySyncOptions;
import com.commercetools.sync.services.CategoryService;
import io.sphere.sdk.categories.Category;
import io.sphere.sdk.categories.CategoryDraft;
import io.sphere.sdk.categories.commands.CategoryCreateCommand;
import io.sphere.sdk.categories.commands.CategoryUpdateCommand;
import io.sphere.sdk.categories.expansion.CategoryExpansionModel;
import io.sphere.sdk.categories.queries.CategoryQuery;
import io.sphere.sdk.categories.queries.CategoryQueryBuilder;
import io.sphere.sdk.categories.queries.CategoryQueryModel;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.queries.QueryPredicate;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.singleton;

/**
 * Implementation of CategoryService interface.
 * TODO: USE graphQL to get only keys. GITHUB ISSUE#84
 */
public final class CategoryServiceImpl extends BaseServiceWithKey<CategoryDraft, Category, CategorySyncOptions,
    CategoryQuery, CategoryQueryModel, CategoryExpansionModel<Category>> implements CategoryService {

    public CategoryServiceImpl(@Nonnull final CategorySyncOptions syncOptions) {
        super(syncOptions);
    }

    @Nonnull
    @Override
    public CompletionStage<Map<String, String>> cacheKeysToIds(@Nonnull final Set<String> categoryKeys) {
        return cacheKeysToIds(
                categoryKeys, keysNotCached -> CategoryQuery
                    .of()
                    .withPredicates(buildCategoryKeysQueryPredicate(keysNotCached)));
    }

    QueryPredicate<Category> buildCategoryKeysQueryPredicate(@Nonnull final Set<String> categoryKeys) {
        final String keysQueryString = categoryKeys.stream()
                                                   .filter(StringUtils::isNotBlank)
                                                   .map(productKey -> format("\"%s\"", productKey))
                                                   .collect(Collectors.joining(","));
        return QueryPredicate.of(format("key in (%s)", keysQueryString));
    }

    @Nonnull
    @Override
    public CompletionStage<Set<Category>> fetchMatchingCategoriesByKeys(@Nonnull final Set<String> categoryKeys) {

        return fetchMatchingResources(categoryKeys,
            () -> CategoryQuery
                .of()
                .plusPredicates(categoryQueryModel -> categoryQueryModel.key().isIn(categoryKeys)));
    }

    @Nonnull
    @Override
    public CompletionStage<Optional<Category>> fetchCategory(@Nullable final String key) {

        return fetchResource(key,
            () -> CategoryQuery.of().plusPredicates(categoryQueryModel -> categoryQueryModel.key().is(key)));
    }

    @Nonnull
    @Override
    public CompletionStage<Optional<String>> fetchCachedCategoryId(@Nonnull final String key) {

        return fetchCachedResourceId(key,
            () -> CategoryQueryBuilder
                .of()
                .plusPredicates(queryModel -> queryModel.key().isIn(singleton(key)))
                .build());
    }

    @Nonnull
    @Override
    public CompletionStage<Optional<Category>> createCategory(@Nonnull final CategoryDraft categoryDraft) {
        return createResource(categoryDraft, CategoryCreateCommand::of);
    }

    @Nonnull
    @Override
    public CompletionStage<Category> updateCategory(@Nonnull final Category category,
                                                    @Nonnull final List<UpdateAction<Category>> updateActions) {
        return updateResource(category, CategoryUpdateCommand::of, updateActions);
    }
}

