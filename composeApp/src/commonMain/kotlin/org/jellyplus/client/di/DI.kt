package org.jellyplus.client.di

import org.jellyplus.client.data.datasource.local.*
import org.jellyplus.client.data.datasource.remote.*
import org.jellyplus.client.data.discovery.PlatformServerDiscovery
import org.jellyplus.client.data.local.createSecureSettings
import org.jellyplus.client.data.remote.JellyfinApi
import org.jellyplus.client.data.repositories.*
import org.jellyplus.client.domain.discovery.IServerDiscovery
import org.jellyplus.client.domain.models.AppDispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import kotlinx.serialization.json.Json
import org.jellyplus.client.domain.repositories.*
import org.jellyplus.client.domain.usecases.*
import org.jellyplus.client.ui.viewmodels.*

val dataModule = module {
    single { AppDispatchers() }
    single { Json { ignoreUnknownKeys = true; isLenient = true; prettyPrint = true } }
    single { createSecureSettings() }
    
    // 1. Storage Helpers (Concrete Classes)
    singleOf(::PersistentSessionLocalDataSource)
    singleOf(::InMemorySessionLocalDataSource)
    singleOf(::MediaLocalDataSource)
    singleOf(::PlayerSettingsLocalDataSource)
    
    // 2. Strategy Repositories
    single<ISessionRepository> { 
        SessionRepository(
            persistentDataSource = get(),
            inMemoryDataSource = get()
        ) 
    }
    
    single { JellyfinApi(sessionRepository = get()) }

    // 3. Remote DataSources
    single<IAuthRemoteDataSource> { AuthRemoteDataSource(get()) }
    single<IMediaRemoteDataSource> { MediaRemoteDataSource(get()) }
    single<IQuickConnectRemoteDataSource> { QuickConnectRemoteDataSource(get()) }

    // 4. Repositories (Standardized)
    single<IAuthenticationRepository> { AuthenticationRepository(get(), get(), get(), get()) }
    single<IMediaRepository> { MediaRepository(get(), get(), get(), get()) }
    single<IQuickConnectRepository> { QuickConnectRepository(get(), get(), get()) }
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
    factoryOf(::MarkItemAsPlayedUseCase)
    factoryOf(::SaveCustomMarkerUseCase)
    factoryOf(::GetWatchHistoryUseCase)
    factoryOf(::GetAutoSkipUseCase)
    factoryOf(::SetAutoSkipUseCase)
    factoryOf(::GetAutoNextUseCase)
    factoryOf(::SetAutoNextUseCase)
    factoryOf(::GetAutoSkipOutroUseCase)
    factoryOf(::SetAutoSkipOutroUseCase)
    factoryOf(::GetPlaybackSpeedUseCase)
    factoryOf(::SetPlaybackSpeedUseCase)
    factoryOf(::GetAutoPictureInPictureUseCase)
    factoryOf(::SetAutoPictureInPictureUseCase)
    factoryOf(::GetIntroMarkersUseCase)
    factoryOf(::DiscoverServersUseCase)
    factoryOf(::ValidateSessionUseCase)
}

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::MoviesViewModel)
    viewModelOf(::TvShowsViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::QuickConnectViewModel)
    viewModelOf(::DiscoveryViewModel)
    viewModelOf(::DownloadsViewModel)
    viewModelOf(::SeriesDetailViewModel)
    viewModelOf(::MovieDetailViewModel)
    viewModelOf(::PlayerViewModel)
    viewModelOf(::PlaybackPreferencesViewModel)
    viewModelOf(::SessionViewModel)
}

fun appModule(): List<Module> = listOf(dataModule, domainModule, viewModelModule)
