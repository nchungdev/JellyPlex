package org.jellyplex.client.di

import org.jellyplex.client.data.datasource.local.IMediaLocalDataSource
import org.jellyplex.client.data.datasource.local.ISessionLocalDataSource
import org.jellyplex.client.data.datasource.local.InMemorySessionLocalDataSource
import org.jellyplex.client.data.datasource.remote.IAuthRemoteDataSource
import org.jellyplex.client.data.discovery.PlatformServerDiscovery
import org.jellyplex.client.data.local.SessionManager
import org.jellyplex.client.data.local.CacheManager
import org.jellyplex.client.data.local.createSecureSettings
import org.jellyplex.client.data.remote.JellyfinApi
import org.jellyplex.client.data.repositories.AuthenticationRepository
import org.jellyplex.client.data.repositories.MediaRepository
import org.jellyplex.client.data.repositories.QuickConnectRepository
import org.jellyplex.client.domain.models.AppDispatchers
import org.jellyplex.client.domain.repositories.IAuthenticationRepository
import org.jellyplex.client.domain.repositories.IMediaRepository
import org.jellyplex.client.domain.repositories.IQuickConnectRepository
import org.jellyplex.client.domain.usecases.*
import org.jellyplex.client.ui.viewmodels.*
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dataModule = module {
    single { AppDispatchers() }
    single { createSecureSettings() }
    singleOf(::SessionManager)
    singleOf(::CacheManager)
    single { JellyfinApi(sessionManager = get()) }

    // DataSources
    // 1. Session DataSources
    single<InMemorySessionLocalDataSource> {
        InMemorySessionLocalDataSource()
    }
    single<ISessionLocalDataSource> {
        org.jellyplex.client.data.datasource.local.SessionLocalDataSource(
            persistentDataSource = get<org.jellyplex.client.data.local.SessionManager>(),
            inMemoryDataSource = get<InMemorySessionLocalDataSource>()
        )
    }

    // 2. Remote DataSources
    single<IAuthRemoteDataSource> {
        org.jellyplex.client.data.datasource.remote.AuthRemoteDataSource(get())
    }
    single<org.jellyplex.client.data.datasource.remote.IMediaRemoteDataSource> {
        org.jellyplex.client.data.datasource.remote.MediaRemoteDataSource(get())
    }
    single<org.jellyplex.client.data.datasource.remote.IQuickConnectRemoteDataSource> {
        org.jellyplex.client.data.datasource.remote.QuickConnectRemoteDataSource(get())
    }

    // 3. Local Media DataSource
    single<IMediaLocalDataSource> {
        org.jellyplex.client.data.datasource.local.MediaLocalDataSource(get())
    }

    // Repositories
    single<IAuthenticationRepository> { AuthenticationRepository(get(), get(), get()) }
    single<IMediaRepository> { MediaRepository(get(), get(), get(), get()) }
    single<IQuickConnectRepository> { QuickConnectRepository(get(), get()) }
    single<org.jellyplex.client.domain.discovery.IServerDiscovery> { PlatformServerDiscovery() }
}

val domainModule = module {
    factoryOf(::RefreshHomeContentUseCase)
    factoryOf(::RefreshMoviesUseCase)
    factoryOf(::RefreshTvShowsUseCase)
    factoryOf(::GetMoviesUseCase)
    factoryOf(::GetTvShowsUseCase)
    factoryOf(::GetHomeContentUseCase)
    factoryOf(::SearchItemsUseCase)
    factoryOf(::LoginUseCase)
    factoryOf(::ResolveStreamConfigUseCase)
    factoryOf(::GetItemDetailsUseCase)
    factoryOf(::GetPeopleUseCase)
    factoryOf(::GetSeasonsUseCase)
    factoryOf(::GetEpisodesUseCase)
    factoryOf(::GetBaseUrlUseCase)
    factoryOf(::UpdateBaseUrlUseCase)
    factoryOf(::GetUserIdUseCase)
    factoryOf(::GetAccessTokenUseCase)
    factoryOf(::InitiateQuickConnectUseCase)
    factoryOf(::PollQuickConnectStatusUseCase)
    factoryOf(::ValidateServerUseCase)
    factoryOf(::HasSessionUseCase)
    factoryOf(::GetIsAuthenticatedUseCase)
    factoryOf(::ClearSessionUseCase)
    factoryOf(::ReportPlaybackStartUseCase)
    factoryOf(::ReportPlaybackProgressUseCase)
    factoryOf(::ReportPlaybackStoppedUseCase)
    factoryOf(::DiscoverServersUseCase)
    factoryOf(::ValidateSessionUseCase)
}

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::MoviesViewModel)
    viewModelOf(::TvShowsViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::QuickConnectViewModel)
    viewModelOf(::DiscoveryViewModel)
    viewModelOf(::DownloadsViewModel)
    viewModelOf(::SeriesDetailViewModel)
    viewModelOf(::MovieDetailViewModel)
    viewModelOf(::PlayerViewModel)
    viewModelOf(::SessionViewModel)
}

fun appModule(): List<Module> = listOf(dataModule, domainModule, viewModelModule)
