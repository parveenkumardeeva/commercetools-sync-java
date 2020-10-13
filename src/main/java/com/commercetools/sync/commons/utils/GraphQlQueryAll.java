package com.commercetools.sync.commons.utils;

import com.commercetools.sync.commons.helpers.BaseGraphQlRequest;
import com.commercetools.sync.commons.helpers.BaseGraphQlResult;
import com.commercetools.sync.commons.models.ResourceKeyId;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.queries.PagedQueryResult;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

final class GraphQlQueryAll<T extends BaseGraphQlResult> {
    private final SphereClient client;
    private final BaseGraphQlRequest<T> baseGraphQlRequest;
    private final long pageSize;

    //private Function<List<T>, S> pageMapper;
    //private final List<S> mappedResultsTillNow;

    private Consumer<Set<ResourceKeyId>> pageConsumer;

    private GraphQlQueryAll(@Nonnull final SphereClient client,
                            @Nonnull final BaseGraphQlRequest<T> baseGraphQlRequest,
                            final long pageSize) {

        this.client = client;
        this.baseGraphQlRequest = baseGraphQlRequest;
        this.pageSize = pageSize;
       //this.mappedResultsTillNow = new ArrayList<>();
    }

    @Nonnull
    private static <T extends BaseGraphQlResult> BaseGraphQlRequest<T> withDefaults(
        @Nonnull final BaseGraphQlRequest<T> query, final long pageSize) {

        //final C withLimit = query.withLimit(pageSize);
        //return !withLimit.sort().isEmpty() ? withLimit : withLimit.withSort(QuerySort.of("id asc"));
        return query;
    }

    @Nonnull
    static <T extends BaseGraphQlResult> GraphQlQueryAll<T> of(
        @Nonnull final SphereClient client,
        @Nonnull final BaseGraphQlRequest<T> baseQuery,
        final int pageSize) {

        return new GraphQlQueryAll<>(client, baseQuery, pageSize);
    }

    /**
     * Given a {@link Function} to a page of resources of type {@code T} that returns a mapped result of type {@code S},
     * this method sets this instance's {@code pageMapper} to the supplied value, then it makes requests to fetch the
     * entire result space of the resource {@code T} on CTP, while applying the function on each fetched page.
     *
     * @param pageMapper the function to apply on each fetched page of the result space.
     * @return a future containing a list of mapped results of type {@code S}, after the function applied all the pages.
     */
//    @Nonnull
//    CompletionStage<List<S>> run(@Nonnull final Function<List<T>, S> pageMapper) {
//        this.pageMapper = pageMapper;
//        final CompletionStage<PagedQueryResult<T>> firstPage = client.execute(baseGraphQlRequest);
//        return queryNextPages(firstPage)
//            .thenApply(voidResult -> this.mappedResultsTillNow);
//    }

    /**
     * Given a {@link Consumer} to a page of resources of type {@code T}, this method sets this instance's
     * {@code pageConsumer} to the supplied value, then it makes requests to fetch the entire result space of the
     * resource {@code T} on CTP, while accepting the consumer on each fetched page.
     *
     * @param pageConsumer the consumer to accept on each fetched page of the result space.
     * @return a future containing void after the consumer accepted all the pages.
     */
    @Nonnull
    CompletionStage<Void> run(@Nonnull final Consumer<Set<ResourceKeyId>> pageConsumer) {
        this.pageConsumer = pageConsumer;
        final CompletionStage<BaseGraphQlResult> firstPage = client.execute(baseGraphQlRequest);
        return queryNextPages(firstPage).thenAccept(voidResult -> { });
    }

    /**
     * Given a completion stage {@code currentPageStage} containing a current page result {@link PagedQueryResult}, this
     * method composes the completion stage by first checking if the result is null or not. If it is not, then it
     * recursivley (by calling itself with the next page's completion stage result) composes to the supplied stage,
     * stages of the all next pages' processing. If there is no next page, then the result of the
     * {@code currentPageStage} would be null and this method would just return a completed future containing
     * containing null result, which in turn signals the last page of processing.
     *
     * @param currentPageStage a future containing a result {@link PagedQueryResult}.
     */
    @Nonnull
    private CompletionStage<Void> queryNextPages(@Nonnull final CompletionStage<BaseGraphQlResult> currentPageStage) {
        return currentPageStage.thenCompose(currentPage ->
            currentPage != null ? queryNextPages(processPageAndGetNext(currentPage)) : completedFuture(null));
    }

    /**
     * Given a page result {@link PagedQueryResult}, this method checks if there are elements in the result (size > 0),
     * then it maps or consumes the resultant list using this instance's {@code pageMapper} or {code pageConsumer}
     * whichever is available. Then it attempts to fetch the next page if it exists and returns a completion stage
     * containing the result of the next page. If there is a next page, then a new future of the next page is returned.
     * If there are no more results, the method returns a completed future containing null.
     *
     * @param page the current page result.
     * @return If there is a next page, then a new future of the next page is returned. If there are no more results,
     *         the method returns a completed future containing null.
     */
    @Nonnull
    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION") // `https://github.com/findbugsproject/findbugs/issues/79
    private CompletionStage<BaseGraphQlResult> processPageAndGetNext(@Nonnull final BaseGraphQlResult page) {
        final Set<ResourceKeyId> currentPageElements = page.getResults();
        if (!currentPageElements.isEmpty()) {
            mapOrConsume(currentPageElements);
            return getNextPageStage(currentPageElements);
        }
        return completedFuture(null);
    }

    /**
     * Given a list of page elements of resource {@code T}, this method checks if this instance's {@code pageConsumer}
     * or {@code pageMapper} is set (not null). The one which is set is then applied on the list of page elements.
     *
     *
     * @param pageElements list of page elements of resource {@code T}.
     */
    private void mapOrConsume(@Nonnull final Set<ResourceKeyId> pageElements) {
        if (pageConsumer != null) {
            pageConsumer.accept(pageElements);
        } else {
            //mappedResultsTillNow.add(pageMapper.apply(pageElements));
        }
    }

    /**
     * Given a list of page elements of resource {@code T}, this method checks if this page is the last page or not by
     * checking if the result size is equal to this instance's {@code pageSize}). If It is, then it means there might be
     * still more results. However, if not, then it means for sure there are no more results and this is the last page.
     * If there is a next page, the id of the last element in the list is fetched and a future is created containing the
     * fetched results which have an id greater than the id of the last element in the list and this future is returned.
     * If there are no more results, the method returns a completed future containing null.
     *
     * @param pageElements list of page elements of resource {@code T}.
     * @return a future containing the fetched results which have an id greater than the id of the last element
     *          in the list.
     */
    @Nonnull
    @SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION") // `https://github.com/findbugsproject/findbugs/issues/79
    private CompletionStage<BaseGraphQlResult> getNextPageStage(@Nonnull final Set<ResourceKeyId> pageElements) {
        if (pageElements.size() == pageSize) {
            String lastElementId = EMPTY;
            Iterator<ResourceKeyId> iterator = pageElements.iterator();
            while(iterator.hasNext()) {
                lastElementId = iterator.next().getId();
            }
            final String queryPredicate = isBlank(lastElementId) ? null : format("id > \\\\\\\"%s\\\\\\\"",
                lastElementId);

            return client.execute(baseGraphQlRequest.withPredicate(baseGraphQlRequest, queryPredicate));
        }
        return completedFuture(null);
    }
}
