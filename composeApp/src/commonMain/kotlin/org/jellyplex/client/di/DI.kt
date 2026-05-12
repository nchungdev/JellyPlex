package org.jellyplex.client.di

import org.jellyplex.client.data.datasource.local.*
import org.jellyplex.client.data.datasource.remote.*
import org.jellyplex.client.data.discovery.PlatformServerDiscovery
import org.jellyplex.client.data.local.createSecureSettings
import org.jellyplex.client.data.remote.JellyfinApi
import org.jellyplex.client.data.repositories.*
import org.jellyplex.client.domain.discovery.IServerDiscovery
import org.jellyplex.client.domain.models.AppDispatchers
import org.jellyplex.client.domain.repositories.*
import org.jellyplex.client.domain.usecases.*
import org.jellyplex.client.ui.viewmodels.*
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import kotlinx.serialization.json.Json

val dataModule = module {
    single { AppDispatchers() }
    single { Json { ignoreUnknownKeys = true; isLenient = true; prettyPrint = true } }
    single { createSecureSettings() }
    
    // 1. Storage Helpers (Concrete Classes)
    singleOf(::PersistentSessionLocalDataSource)
    singleOf(::InMemorySessionLocalDataSource)
    singleOf(::MediaLocalDataSource)
    
    // 2. Strategy Repositories
    single<ISessionRepository> { 
        SessionRepository(
            persistentDataSource = get<PersistentSessionLocalDataSource>(),
            inMemoryDataSource = get<InMemorySessionLocalDataSource>()
        ) 
    }
    
    single { JellyfinApi(sessionRepository = get()) }

    // 3. Remote DataSources
    single<IAuthRemoteDataSource> { AuthRemoteDataSource(get()) }
    single<IMediaRemoteDataSource> { MediaRemoteDataSource(get()) }
    single<IQuickConnectRemoteDataSource> { QuickConnectRemoteDataSource(get()) }

    // 4. Final Feature Repositories
    single<IAuthenticationRepository> { AuthenticationRepository(get(), get(), get()) }
    single<IMediaRepository> { MediaRepository(get(), get(), get(), get()) }
    single<IQuickConnectRepository> { QuickConnectRepository(get(), get()) }
    single<IServerDiscovery> { PlatformServerDiscovery() }
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
